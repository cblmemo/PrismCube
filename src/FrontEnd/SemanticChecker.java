package FrontEnd;

import AST.*;
import AST.DefineNode.*;
import AST.ExpressionNode.*;
import AST.PrimaryNode.*;
import AST.StatementNode.*;
import AST.TypeNode.*;
import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.Memory;
import Utility.Scope.*;
import Utility.Type.ArrayType;
import Utility.Type.ClassType;
import Utility.Type.Type;
import Utility.error.SemanticError;

import java.util.Objects;

import static Debug.MemoLog.log;

public class SemanticChecker implements ASTVisitor {
    private GlobalScope globalScope;
    private Scope currentScope;

    public void check(Memory memory) {
        log.Infof("Check started.\n");

        currentScope = globalScope = memory.getGlobalScope();
        visit(memory.getASTRoot());

        log.Infof("Check started.\n");
    }

    private void throwError(String message, ASTNode node) {
        throw new SemanticError("[check] " + message, node.getCursor());
    }

    @Override
    public void visit(ProgramNode node) {
        node.getDefines().forEach(define -> {
            define.accept(this);
        });
    }

    @Override
    public void visit(ClassDefineNode node) {
        if (node.isInvalid()) throwError(node.getMessage(), node);
        if (!globalScope.hasThisClass(node.getClassName()))
            throwError("cannot find classes. maybe symbol collector has some bugs.", node);
        currentScope = globalScope.getClass(node.getClassName()).getClassScope();
        if (node.hasCustomConstructor()) node.getConstructor().accept(this);
        node.getMembers().forEach(member -> {
            member.accept(this);
        });
        node.getMethods().forEach(method -> {
            method.accept(this);
        });
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(VariableDefineNode node) {
        if (node.getType().toType(globalScope) instanceof ArrayType) {
            if (!globalScope.hasThisClass(((ArrayType) node.getType().toType(globalScope)).getRootElementType().getTypeName()))
                throwError("undefined type", node);
        } else {
            if (!globalScope.hasThisClass(node.getType().getTypeName()))
                throwError("undefined type", node);
        }
        node.getSingleDefines().forEach(singleDefine -> {
            singleDefine.accept(this);
        });
    }

    @Override
    public void visit(SingleVariableDefineNode node) {
        // already checked type at VariableDefineNode
        if (!(currentScope instanceof ClassScope)) {
            if (currentScope.hasVariable(node.getVariableNameStr()))
                throwError("repeated variable name", node);
            Type variableType = node.getType().toType(globalScope);
            currentScope.addVariable(new VariableEntity(variableType, node.getVariableNameStr(), node.getCursor()));
            node.getVariableName().setExpressionType(variableType);
        }
        if (node.hasInitializeValue()) {
            node.getInitializeValue().accept(this);
            if (node.getType() instanceof ArrayTypeNode) {
                if (node.getInitializeValue().getExpressionType() instanceof ArrayType) {
                    if (((ArrayTypeNode) node.getType()).getDimension() != ((ArrayType) node.getInitializeValue().getExpressionType()).getDimension())
                        throwError("array variable define with unmatched dimension", node);
                    if (!Objects.equals(((ArrayTypeNode) node.getType()).getRootTypeName(), ((ArrayType) node.getInitializeValue().getExpressionType()).getRootElementType().getTypeName()))
                        throwError("array variable define with unmatched root element type", node);
                } else throwError("array variable define with non-array initialize value", node);
            } else {
                if (!Objects.equals(node.getType().getTypeName(), node.getInitializeValue().getExpressionType().getTypeName()))
                    throwError("variable define with unmatched type", node);
            }
        }
    }

    @Override
    public void visit(ConstructorDefineNode node) {
        // visit ConstructorDefineNode represent class has custom constructor
        currentScope = ((ClassScope) currentScope).getConstructor().getConstructorScope();
        node.getStatements().forEach(statement -> {
            statement.accept(this);
        });
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(FunctionDefineNode node) {
        FunctionEntity function = currentScope.getFunctionRecursively(node.getFunctionName());
        if (function == null)
            throwError("undefined function " + node.getFunctionName(), node);
        currentScope = function.getFunctionScope();
        node.getStatements().forEach(statement -> {
            statement.accept(this);
        });
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(ParameterDefineNode node) {
        // not suppose execute this function.
    }

    @Override
    public void visit(BlockStatementNode node) {
        boolean insideNewScope = currentScope instanceof BranchScope || currentScope instanceof LoopScope;
        if (!insideNewScope) currentScope = new BracesScope(currentScope);
        node.getStatements().forEach(statement -> {
            statement.accept(this);
        });
        if (!insideNewScope) currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(IfStatementNode node) {
        node.getConditionExpression().accept(this);
        if (!node.getConditionExpression().getExpressionType().isBool())
            throwError("if statement with non-bool condition expression", node);
        currentScope = new BracesScope(currentScope);
        node.getTrueStatement().accept(this);
        currentScope = currentScope.getParentScope();
        if (node.hasElse()) {
            currentScope = new BranchScope(currentScope);
            node.getFalseStatement().accept(this);
            currentScope = currentScope.getParentScope();
        }
    }

    @Override
    public void visit(ForStatementNode node) {
        if (node.hasInitializeExpression()) node.getInitializeExpression().accept(this);
        if (node.hasConditionExpression()) node.getConditionExpression().accept(this);
        if (node.hasStepExpression()) node.getStepExpression().accept(this);
        currentScope = new LoopScope(currentScope);
        node.getLoopBody().accept(this);
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(WhileStatementNode node) {
        node.getConditionExpression().accept(this);
        currentScope = new LoopScope(currentScope);
        node.getLoopBody().accept(this);
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(ReturnStatementNode node) {
        if (!currentScope.insideMethod())
            throwError("return statement not in method scope", node);
        MethodScope methodScope = currentScope.getMethodScope();
        if (node.hasReturnValue()) {
            node.getReturnValue().accept(this);
            if (methodScope instanceof ConstructorScope)
                throwError("return statement in constructor has a return value", node);
            if (!Objects.equals(node.getReturnValue().getExpressionType().getTypeName(), ((FunctionScope) methodScope).getReturnType().getTypeName()))
                throwError("return value unmatch with return type", node);
        } else {
            if (methodScope instanceof FunctionScope) {
                if (!((FunctionScope) methodScope).getReturnType().isVoid())
                    throwError("return statement has no return value in non-void function", node);
            }
        }
    }

    @Override
    public void visit(BreakStatementNode node) {
        if (!(currentScope instanceof LoopScope))
            throwError("break statement outside loop body", node);
    }

    @Override
    public void visit(ContinueStatementNode node) {
        if (!(currentScope instanceof LoopScope))
            throwError("break statement outside loop body", node);
    }

    @Override
    public void visit(ExpressionStatementNode node) {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(EmptyStatementNode node) {
        // do nothing
    }

    @Override
    public void visit(NewTypeExpressionNode node) {
        if (node.isInvalid())
            throwError("error new array definition", node);
        node.getDimensionExpressions().forEach(expression -> {
            expression.accept(this);
            if (!expression.getExpressionType().isInt())
                throwError("new array dimension expression has a non-int expression type", node);
        });
        String rootElementType = node.getRootElementType().getTypeName();
        if (!globalScope.hasThisClass(rootElementType))
            throwError("undefined root type of new array expression", node);
        node.setExpressionType(((ClassType) node.getRootElementType().toType(globalScope)).toArrayType(node.getDimension(), globalScope));
    }

    @Override
    public void visit(MemberAccessExpressionNode node) {
        node.getInstance().accept(this);
        if (node.getInstance().getExpressionType() instanceof ClassType) {
            if (!((ClassType) node.getInstance().getExpressionType()).getClassScope().hasIdentifier(node.getMemberName()))
                throwError("undefined member name", node);
            else {
                if (node.isAccessMethod()) node.setExpressionType(((ClassType) node.getInstance().getExpressionType()).getClassScope().getFunctionReturnType(node.getMemberName()));
                else node.setExpressionType(((ClassType) node.getInstance().getExpressionType()).getClassScope().getVariableType(node.getMemberName()));
            }
        } else {
            if (!Objects.equals(node.getMemberName(), "size"))
                throwError("member access to array type and member name is not \"size\"", node);
            node.setExpressionType(globalScope.getClass("int"));
        }
    }

    @Override
    public void visit(LambdaExpressionNode node) {
        // todo
    }

    @Override
    public void visit(FunctionCallExpressionNode node) {
        if (node.isInvalid())
            throwError("function call with function expression neither member access nor identifier", node);
        node.getFunction().accept(this);
        node.getArguments().forEach(argument -> {
            argument.accept(this);
        });
        FunctionEntity function;
        if (node.isClassMethod()) {
            if (node.getInstance().getExpressionType() instanceof ClassType) {
                ClassType methodClass = (ClassType) node.getInstance().getExpressionType();
                node.setExpressionType(methodClass.getClassScope().getFunctionReturnType(node.getFunctionName()));
                function = methodClass.getClassScope().getFunction(node.getFunctionName());
            } else {
                if (!Objects.equals(node.getFunctionName(), "size"))
                    throwError("call non-size function " + node.getFunctionName() + " to array type", node);
                if (node.getArguments().size() != 0)
                    throwError("call size function with argument(s)", node);
                node.setExpressionType(globalScope.getClass("int"));
                return;
            }
        } else {
            node.setExpressionType(currentScope.getFunctionReturnTypeRecursively(node.getFunctionName()));
            function = currentScope.getFunctionRecursively(node.getFunctionName());
        }
        if (function == null)
            throwError("undefined function " + node.getFunctionName(), node);
        if (function.getFunctionScope().getParameters().size() != node.getArguments().size())
            throwError("function call with unmatched argument number", node);
        for (int i = 0; i < node.getArguments().size(); i++) {
            if (node.getArgument(i).getExpressionType() == null)
                throwError("aaa", node);
            if (!function.getParameter(i).getVariableType().equal(node.getArgument(i).getExpressionType()))
                throwError("function call " + i + "-th argument type unmatch", node);
        }
    }

    @Override
    public void visit(AddressingExpressionNode node) {
        node.getArray().accept(this);
        if (!node.getArray().getExpressionType().isArrayType())
            throwError("addressing to non-array type", node);
        node.getIndex().accept(this);
        if (!node.getIndex().getExpressionType().isInt())
            throwError("addressing with non-int index", node);
        ClassType rootElementType = ((ArrayType) node.getArray().getExpressionType()).getRootElementType();
        if (((ArrayType) node.getArray().getExpressionType()).getDimension() == 1) node.setExpressionType(rootElementType);
        else node.setExpressionType(new ArrayType(rootElementType, ((ArrayType) node.getArray().getExpressionType()).getDimension() - 1));
    }

    @Override
    public void visit(PostCrementExpressionNode node) {
        node.getLhs().accept(this);
        if (!node.getLhs().getExpressionType().isInt())
            throwError("post in/decrement to non-int type", node);
        if (!node.getLhs().isLeftValue())
            throwError("post in/decrement to non-assignable type", node);
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        node.getRhs().accept(this);
        switch (node.getOp()) {
            case "++", "--" -> {
                if (!node.getRhs().getExpressionType().isInt())
                    throwError("unary op " + node.getOp() + " has a non-int operate object", node);
                if (!node.getRhs().isLeftValue())
                    throwError("unary op " + node.getOp() + " has a non-assignable operate object", node);
            }
            case "+", "-", "~" -> {
                if (!node.getRhs().getExpressionType().isInt())
                    throwError("unary op " + node.getOp() + " has a non-int operate object", node);
            }
            case "!" -> {
                if (!node.getRhs().getExpressionType().isBool())
                    throwError("unary op " + node.getOp() + " has a non-bool operate object", node);
            }
        }
        node.setExpressionType(node.getRhs().getExpressionType());
    }

    @Override
    public void visit(BinaryExpressionNode node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
        Type lhsType = node.getLhs().getExpressionType();
        Type rhsType = node.getRhs().getExpressionType();
        switch (node.getOp()) {
            case "*", "/", "%", "-", "<<", ">>", "&", "^", "|" -> {
                if (!lhsType.isInt() || !rhsType.isInt())
                    throwError("binary op " + node.getOp() + " has non-int operate object", node);
                node.setExpressionType(lhsType);
            }
            case "+" -> {
                if (!Objects.equals(lhsType.getTypeName(), rhsType.getTypeName()))
                    throwError("binary op " + node.getOp() + " has two unmatch operate object " + lhsType.getTypeName() + " and " + rhsType.getTypeName(), node);
                if (!lhsType.isInt() && !rhsType.isString())
                    throwError("binary op " + node.getOp() + " has non-int and non-string operate object", node);
                node.setExpressionType(lhsType);
            }
            case "<", "<=", ">", ">=" -> {
                if (!Objects.equals(lhsType.getTypeName(), rhsType.getTypeName()))
                    throwError("binary op " + node.getOp() + " has two unmatch operate object " + lhsType.getTypeName() + " and " + rhsType.getTypeName(), node);
                if (!lhsType.isInt() && !lhsType.isString())
                    throwError("binary op " + node.getOp() + " has non-int and non-string operate object", node);
                node.setExpressionType(globalScope.getClass("bool"));
            }
            case "==", "!=" -> {
                if (!(node.getLhs().getExpressionType().isNull() && node.getRhs().getExpressionType().isArrayType())
                        || !(node.getLhs().getExpressionType().isArrayType() && node.getRhs().getExpressionType().isNull())) {
                    if (!Objects.equals(node.getLhs().getExpressionType().getTypeName(), node.getRhs().getExpressionType().getTypeName()))
                        throwError("binary op " + node.getOp() + " has two unmatch operate object", node);
                    if (!node.getLhs().getExpressionType().isInt() && !node.getLhs().getExpressionType().isString())
                        throwError("binary op " + node.getOp() + " has non-int and non-string operate object", node);
                }
                node.setExpressionType(globalScope.getClass("bool"));
            }
            case "&&", "||" -> {
                if (!Objects.equals(node.getLhs().getExpressionType().getTypeName(), node.getRhs().getExpressionType().getTypeName()))
                    throwError("binary op " + node.getOp() + " has two unmatch operate object", node);
                if (!node.getLhs().getExpressionType().isBool())
                    throwError("binary op " + node.getOp() + " has non-bool operate object", node);
                node.setExpressionType(globalScope.getClass("bool"));
            }
        }
    }

    @Override
    public void visit(AssignExpressionNode node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
        if (!node.getLhs().isLeftValue())
            throwError("assign to nonassignable object", node);
        Type lhsType = node.getLhs().getExpressionType();
        Type rhsType = node.getRhs().getExpressionType();
        if (!(lhsType.isArrayType()) && rhsType.isNull() || lhsType.isNull() && rhsType.isArrayType()) {
            if (!Objects.equals(lhsType.getTypeName(), rhsType.getTypeName()))
                throwError("assign one different type object to another", node);
        }
        node.setExpressionType(node.getLhs().getExpressionType());
    }

    @Override
    public void visit(ThisPrimaryNode node) {
        if (!currentScope.insideClassMethod())
            throwError("this expression outside method scope", node);
        node.setExpressionType(globalScope.getClass(currentScope.getUpperClassScope().getClassName()));
    }

    @Override
    public void visit(IdentifierPrimaryNode node) {
        if (!currentScope.hasIdentifierRecursively(node.getIdentifier()))
            throwError("undefined identifier " + node.getIdentifier(), node);
        if (node.isVariable()) node.setExpressionType(currentScope.getVariableTypeRecursively(node.getIdentifier()));
        else if (node.isFunction()) node.setExpressionType(currentScope.getFunctionReturnTypeRecursively(node.getIdentifier()));
        else throwError("identifier primary node " + node.getIdentifier() + "neither variable nor function. should not throw this.", node);
    }

    @Override
    public void visit(NumericalConstantPrimaryNode node) {
        node.setExpressionType(globalScope.getClass("int"));
    }

    @Override
    public void visit(BoolConstantPrimaryNode node) {
        node.setExpressionType(globalScope.getClass("bool"));
    }

    @Override
    public void visit(StringConstantPrimaryNode node) {
        node.setExpressionType(globalScope.getClass("string"));
    }

    @Override
    public void visit(NullConstantPrimaryNode node) {
        node.setExpressionType(globalScope.getClass("null"));
    }

    @Override
    public void visit(ReturnTypeNode node) {
        if (!globalScope.hasThisClass(node.getTypeName()))
            throwError("undefined return type", node);
    }

    @Override
    public void visit(SpecialTypeNode node) {
        if (!globalScope.hasThisClass(node.getTypeName()))
            throwError("undefined special type", node);
    }

    @Override
    public void visit(ArrayTypeNode node) {
        if (!globalScope.hasThisClass(node.getRootTypeName()))
            throwError("undefined array root type", node);
    }

    @Override
    public void visit(ClassTypeNode node) {
        if (!globalScope.hasThisClass(node.getTypeName()))
            throwError("undefined class type", node);
    }

    @Override
    public void visit(BuiltinTypeNode node) {
        if (!globalScope.hasThisClass(node.getTypeName()))
            throwError("undefined builtin type", node);
        if (!globalScope.getClass(node.getTypeName()).isBuiltinType())
            throwError("non-builtin builtin type node", node);
    }
}

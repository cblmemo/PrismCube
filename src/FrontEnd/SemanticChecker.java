package FrontEnd;

import AST.ASTNode;
import AST.ASTVisitor;
import AST.DefineNode.*;
import AST.ExpressionNode.*;
import AST.PrimaryNode.*;
import AST.ProgramNode;
import AST.StatementNode.*;
import AST.TypeNode.*;
import Memory.Memory;
import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.Scope.*;
import Utility.Type.ArrayType;
import Utility.Type.ClassType;
import Utility.Type.Type;
import Utility.error.SemanticError;

import java.util.Objects;

import static Debug.MemoLog.log;

/**
 * This class checks semantic for source code,
 * and throw an error if checks failed.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class SemanticChecker implements ASTVisitor {
    private GlobalScope globalScope;
    private Scope currentScope;

    public void check(Memory memory) {
        log.Infof("Check started.\n");
        currentScope = globalScope = memory.getGlobalScope();
        visit(memory.getASTRoot());
        log.Infof("Check finished.\n");
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
        if (node.hasInitializeValue()) {
            node.getInitializeValue().accept(this);
            if (node.getType() instanceof ArrayTypeNode) {
                Type initializeValueType = node.getInitializeValue().getExpressionType();
                if (initializeValueType instanceof ArrayType) {
                    if (((ArrayTypeNode) node.getType()).getDimension() != ((ArrayType) node.getInitializeValue().getExpressionType()).getDimension())
                        throwError("array variable define with unmatched dimension", node);
                    if (!Objects.equals(((ArrayTypeNode) node.getType()).getRootTypeName(), ((ArrayType) node.getInitializeValue().getExpressionType()).getRootElementType().getTypeName()))
                        throwError("array variable define with unmatched root element type", node);
                } else if (!initializeValueType.isNull()) throwError("array variable define with non-array initialize value", node);
            } else {
                if (!Objects.equals(node.getType().getTypeName(), node.getInitializeValue().getExpressionType().getTypeName()))
                    if (!(node.getType().toType(globalScope).isNullAssignable() && node.getInitializeValue().getExpressionType().isNull()))
                        throwError("variable define with unmatched type", node);
            }
        }
        // add variable at current scope in the end to avoid int x = f(x); passed check (x is an undefined variable)
        if (!(currentScope instanceof ClassScope)) {
            if (currentScope.hasVariable(node.getVariableNameStr()))
                throwError("repeated variable name", node);
            Type variableType = node.getType().toType(globalScope);
            currentScope.addVariable(new VariableEntity(variableType, node.getVariableNameStr(), node.getCursor()));
            node.getVariableName().setExpressionType(variableType);
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
        if (currentScope instanceof ClassScope)
            if (Objects.equals(function.getEntityName(), ((ClassScope) currentScope).getClassName()))
                throwError("constructor cannot have return type", node);
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
        // avoid if (...) {} and while (...) {} creates two layer of scope
        if (!insideNewScope) currentScope = currentScope.createBracesScope(node);
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
        currentScope = currentScope.createBranchScope(node);
        node.getTrueStatement().accept(this);
        currentScope = currentScope.getParentScope();
        if (node.hasElse()) {
            currentScope = currentScope.createBranchScope(node);
            node.getFalseStatement().accept(this);
            currentScope = currentScope.getParentScope();
        }
    }

    @Override
    public void visit(ForStatementNode node) {
        if (node.hasInitializeExpression()) node.getInitializeExpression().accept(this);
        if (node.hasConditionExpression()) {
            node.getConditionExpression().accept(this);
            if (!node.getConditionExpression().getExpressionType().isBool())
                throwError("non-bool for condition expression", node);
        }
        if (node.hasStepExpression()) node.getStepExpression().accept(this);
        currentScope = currentScope.createLoopScope(node);
        node.getLoopBody().accept(this);
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(WhileStatementNode node) {
        node.getConditionExpression().accept(this);
        currentScope = currentScope.createLoopScope(node);
        node.getLoopBody().accept(this);
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(ReturnStatementNode node) {
        if (!currentScope.insideMethod())
            throwError("return statement not in method scope", node);
        MethodScope methodScope = currentScope.getMethodScope();
        if (methodScope instanceof FunctionScope && ((FunctionScope) methodScope).isLambdaScope()) {
            // lambda function need to infer expression type from return type.
            if (node.hasReturnValue()) {
                node.getReturnValue().accept(this);
                ((FunctionScope) methodScope).setReturnType(node.getReturnValue().getExpressionType());
            } else ((FunctionScope) methodScope).setReturnType(globalScope.getClass("void"));
        } else {
            // normal function need to check whether return value type matched with function return type.
            if (node.hasReturnValue()) {
                node.getReturnValue().accept(this);
                if (methodScope instanceof FunctionScope) {
                    if (!(node.getReturnValue().getExpressionType().isNull() && ((FunctionScope) methodScope).getReturnType().isNullAssignable()))
                        if (!Objects.equals(node.getReturnValue().getExpressionType().getTypeName(), ((FunctionScope) methodScope).getReturnType().getTypeName()))
                            throwError("return value of (" + node.getReturnValue().getExpressionType().getTypeName() + ") unmatch with return type (" + ((FunctionScope) methodScope).getReturnType().getTypeName() + ")", node);
                } else throwError("return statement in constructor has a return value", node);

            } else {
                if (methodScope instanceof FunctionScope)
                    if (!((FunctionScope) methodScope).getReturnType().isVoid())
                        throwError("return statement has no return value in non-void function", node);
            }
        }
    }

    @Override
    public void visit(BreakStatementNode node) {
        if (!currentScope.insideLoop())
            throwError("break statement outside loop body", node);
    }

    @Override
    public void visit(ContinueStatementNode node) {
        if (!currentScope.insideLoop())
            throwError("continue statement outside loop body", node);
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
        // root element type must instanceof ClassType.
        if (node.getDimension() != 0) node.setExpressionType(((ClassType) node.getRootElementType().toType(globalScope)).toArrayType(node.getDimension(), globalScope));
        else node.setExpressionType(node.getRootElementType().toType(globalScope));
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
            // array type can only access to method .size()
            if (!Objects.equals(node.getMemberName(), "size"))
                throwError("member access to array type and member name is not \"size\"", node);
            node.setExpressionType(globalScope.getClass("int"));
        }
    }

    @Override
    public void visit(LambdaExpressionNode node) {
        // don't need to store FunctionScope since lambda only appear in semantic phase
        currentScope = new FunctionScope(null, currentScope);
        ((FunctionScope) currentScope).setLambdaScope();
        if (node.hasParameters()) {
            if (node.getParameters().size() != node.getArguments().size())
                throwError("lambda function call with unmatched argument number", node);
            node.getParameters().forEach(parameter -> {
                if (Objects.equals(parameter.getType().getTypeName(), "void"))
                    throwError("void type parameter", node);
                ((FunctionScope) currentScope).addParameter(new VariableEntity(parameter.getType().toType(globalScope), parameter.getParameterName(), parameter.getCursor()));
            });
            node.getArguments().forEach(argument -> {
                argument.accept(this);
            });
            for (int i = 0; i < node.getParameters().size(); i++) {
                if (!(node.getArgument(i).getExpressionType().isNull() && ((FunctionScope) currentScope).getParameter(i).getVariableType().isNullAssignable()))
                    if (!((FunctionScope) currentScope).getParameter(i).getVariableType().equal(node.getArgument(i).getExpressionType()))
                        throwError("function call " + i + "-th argument has (" + node.getArgument(i).getExpressionType().getTypeName() + ") type, unmatched with (" + ((FunctionScope) currentScope).getParameter(i).getVariableType().getTypeName() + ")", node);
            }
        } else {
            if (node.getArguments().size() != 0)
                throwError("call non-parameter lambda function with arguments", node);
        }
        node.getStatements().forEach(statement -> {
            statement.accept(this);
        });
        // infer expression type from return statement inside
        node.setExpressionType(((FunctionScope) currentScope).getReturnType());
        currentScope = currentScope.getParentScope();
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
        // get function entity
        if (node.isClassMethod()) {
            if (node.getInstance().getExpressionType() instanceof ClassType) {
                ClassType methodClass = (ClassType) node.getInstance().getExpressionType();
                node.setExpressionType(methodClass.getClassScope().getFunctionReturnType(node.getFunctionName()));
                function = methodClass.getClassScope().getFunction(node.getFunctionName());
            } else {
                if (!Objects.equals(node.getFunctionName(), "size"))
                    throwError("call non-size function (" + node.getFunctionName() + ") to array type", node);
                if (node.getArguments().size() != 0)
                    throwError("call size function with argument(s)", node);
                node.setExpressionType(globalScope.getClass("int"));
                return;
            }
        } else {
            node.setExpressionType(currentScope.getFunctionReturnTypeRecursively(node.getFunctionName()));
            function = currentScope.getFunctionRecursively(node.getFunctionName());
        }
        // check function existence and parameters
        if (function == null)
            throwError("undefined function " + node.getFunctionName(), node);
        if (function.getFunctionScope().getParameters().size() != node.getArguments().size())
            throwError("function call with unmatched argument number", node);
        for (int i = 0; i < function.getFunctionScope().getParameters().size(); i++) {
            if (!(node.getArgument(i).getExpressionType().isNull() && function.getParameter(i).getVariableType().isNullAssignable()))
                if (!function.getParameter(i).getVariableType().equal(node.getArgument(i).getExpressionType()))
                    throwError("function call " + i + "-th argument has (" + node.getArgument(i).getExpressionType().getTypeName() + ") type, unmatched with (" + function.getParameter(i).getVariableType().getTypeName() + ")", node);
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
        // addressing decrease dimension
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
        node.setExpressionType(node.getLhs().getExpressionType());
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        node.getRhs().accept(this);
        Type rhsType = node.getRhs().getExpressionType();
        switch (node.getOp()) {
            case "++", "--" -> {
                if (!rhsType.isInt())
                    throwError("unary op (" + node.getOp() + ") has a non-int operate object", node);
                if (!node.getRhs().isLeftValue())
                    throwError("unary op (" + node.getOp() + ") has a non-assignable operate object", node);
            }
            case "+", "-", "~" -> {
                if (!rhsType.isInt())
                    throwError("unary op (" + node.getOp() + ") has a non-int operate object", node);
            }
            case "!" -> {
                if (!rhsType.isBool())
                    throwError("unary op (" + node.getOp() + ") has a non-bool operate object", node);
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
                    throwError("binary op (" + node.getOp() + ") has non-int operate object", node);
                node.setExpressionType(lhsType);
            }
            case "+" -> {
                if (!Objects.equals(lhsType.getTypeName(), rhsType.getTypeName()))
                    throwError("binary op (" + node.getOp() + ") has two unmatch operate object " + lhsType.getTypeName() + " and " + rhsType.getTypeName(), node);
                if (!lhsType.isInt() && !rhsType.isString())
                    throwError("binary op (" + node.getOp() + ") has non-int and non-string operate object", node);
                node.setExpressionType(lhsType);
            }
            case "<", "<=", ">", ">=" -> {
                if (!Objects.equals(lhsType.getTypeName(), rhsType.getTypeName()))
                    throwError("binary op (" + node.getOp() + ") has two unmatch operate object " + lhsType.getTypeName() + " and " + rhsType.getTypeName(), node);
                if (!lhsType.isInt() && !lhsType.isString())
                    throwError("binary op (" + node.getOp() + ") has non-int and non-string operate object", node);
                node.setExpressionType(globalScope.getClass("bool"));
            }
            case "==", "!=" -> {
                // array == null || class == null
                if (!(lhsType.isNull() || rhsType.isNull())) {
                    if (!Objects.equals(lhsType.getTypeName(), rhsType.getTypeName()))
                        throwError("binary op (" + node.getOp() + ") has two unmatch operate object", node);
                    if (!(lhsType.isInt() || lhsType.isString() || lhsType.isBool()))
                        throwError("binary op (" + node.getOp() + ") has non-int, non-string, non-bool and non-null operate object", node);
                }
                node.setExpressionType(globalScope.getClass("bool"));
            }
            case "&&", "||" -> {
                if (!Objects.equals(lhsType.getTypeName(), rhsType.getTypeName()))
                    throwError("binary op (" + node.getOp() + ") has two unmatch operate object", node);
                if (!lhsType.isBool())
                    throwError("binary op (" + node.getOp() + ") has non-bool operate object", node);
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
        if (!(rhsType.isNull() && (lhsType.isNullAssignable())))
            if (!Objects.equals(lhsType.getTypeName(), rhsType.getTypeName()))
                throwError("assign (" + rhsType.getTypeName() + ") type object to (" + lhsType.getTypeName() + ") variable", node);
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
            throwError("undefined identifier (" + node.getIdentifier() + ")", node);
        if (node.isVariable()) node.setExpressionType(currentScope.getVariableTypeRecursively(node.getIdentifier()));
        else if (node.isFunction()) node.setExpressionType(currentScope.getFunctionReturnTypeRecursively(node.getIdentifier()));
        else throwError("identifier primary node (" + node.getIdentifier() + ") neither variable nor function. should not throw this.", node);
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

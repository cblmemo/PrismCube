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
import Utility.Entity.ConstructorEntity;
import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.Scope.*;
import Utility.Type.ClassType;
import Utility.error.SemanticError;

import java.util.Objects;

import static Debug.MemoLog.log;

/**
 * This class collects all symbols that support forward
 * reference, as well as function parameters.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class SymbolCollector implements ASTVisitor {
    private GlobalScope globalScope;
    private Scope currentScope;
    private boolean firstTime;

    private void throwError(String message, ASTNode node) {
        throw new SemanticError("[collect] " + message, node.getCursor());
    }

    /**
     * This method collects symbols and store them
     * in global scope inside memory.
     *
     * @see Memory
     */
    public void collect(Memory memory) {
        log.Infof("Symbol collect started.\n");

        currentScope = globalScope = memory.getGlobalScope();

        // the first time we collect:
        // (0) class define;
        // (1) function define;
        // (2) method define.
        firstTime = true;
        visit(memory.getASTRoot());

        // the second time we collect:
        // (0) member variable;
        // (1) function parameter;
        // (2) function return type,
        // since symbols above have type attribute,
        // and that type maybe some classes defined
        // under those symbols.
        firstTime = false;
        visit(memory.getASTRoot());

        log.Infof("Symbol collect finished.\n");
    }

    @Override
    public void visit(ProgramNode node) {
        if (firstTime) {
            // avoid repeat judge
            if (node.isInvalid())
                throwError("main function error: " + node.getMessage(), node);
        }
        node.getDefines().forEach(define -> {
            define.accept(this);
        });
    }

    @Override
    public void visit(ClassDefineNode node) {
        if (firstTime) {
            if (node.isInvalid())
                throwError("error class with error message: " + node.getMessage(), node);
            if (currentScope != globalScope)
                throwError("class define not in global scope", node);
            if (globalScope.hasFunction(node.getClassName()))
                throwError("class name conflict with existed function", node);
            currentScope = new ClassScope(globalScope);
            ClassType currentClass = new ClassType(node.getClassName());
            if (node.hasCustomConstructor()) {
                node.getConstructor().accept(this);
            }
            node.getMethods().forEach(function -> {
                function.accept(this);
            });
            currentClass.setClassScope((ClassScope) currentScope);
            currentScope = currentScope.getParentScope();
            globalScope.addClass(node.getClassName(), currentClass);
        } else {
            if (globalScope.hasVariable(node.getClassName()))
                throwError("class name conflict with existed variable", node);
            ClassType currentClass = globalScope.getClass(node.getClassName());
            node.getMembers().forEach(member -> {
                member.getSingleDefines().forEach(singleDefine -> {
                    currentClass.addMember(new VariableEntity(singleDefine.getType().toType(globalScope), singleDefine.getVariableNameStr(), singleDefine.getCursor()));
                });
            });
            // enter new scope helps define member variables and methods inside class scope.
            currentScope = globalScope.getClass(node.getClassName()).getClassScope();
            node.getMethods().forEach(function -> {
                function.accept(this);
            });
            currentScope = currentScope.getParentScope();
        }
    }

    @Override
    public void visit(VariableDefineNode node) {

    }

    @Override
    public void visit(SingleVariableDefineNode node) {

    }

    @Override
    public void visit(ConstructorDefineNode node) {
        if (firstTime) {
            if (!(currentScope instanceof ClassScope))
                throwError("constructor define outside class scope", node);
            ConstructorEntity constructor = new ConstructorEntity(node.getConstructorName(), node.getCursor());
            constructor.setConstructorScope(new ConstructorScope(currentScope));
            ((ClassScope) currentScope).setConstructor(constructor);
        }
    }

    @Override
    public void visit(FunctionDefineNode node) {
        if (firstTime) {
            if (globalScope.hasThisClass(node.getFunctionName()))
                throwError("function name " + node.getFunctionName() + " conflict with existed class", node);
            FunctionEntity function = new FunctionEntity(new FunctionScope(null, currentScope), node.getFunctionName(), node.getCursor());
            currentScope.addFunction(function);
        } else {
            FunctionEntity function = currentScope.getFunction(node.getFunctionName());
            function.setReturnType(node.getReturnType().toType(globalScope));
            node.getParameters().forEach(parameter -> {
                if (Objects.equals(parameter.getType().getTypeName(), "void"))
                    throwError("void type parameter", node);
                function.addParameter(new VariableEntity(parameter.getType().toType(globalScope), parameter.getParameterName(), parameter.getCursor()));
            });
        }
    }

    @Override
    public void visit(ParameterDefineNode node) {

    }

    @Override
    public void visit(BlockStatementNode node) {

    }

    @Override
    public void visit(IfStatementNode node) {

    }

    @Override
    public void visit(ForStatementNode node) {

    }

    @Override
    public void visit(WhileStatementNode node) {

    }

    @Override
    public void visit(ReturnStatementNode node) {

    }

    @Override
    public void visit(BreakStatementNode node) {

    }

    @Override
    public void visit(ContinueStatementNode node) {

    }

    @Override
    public void visit(ExpressionStatementNode node) {

    }

    @Override
    public void visit(EmptyStatementNode node) {

    }

    @Override
    public void visit(NewTypeExpressionNode node) {

    }

    @Override
    public void visit(MemberAccessExpressionNode node) {

    }

    @Override
    public void visit(LambdaExpressionNode node) {

    }

    @Override
    public void visit(FunctionCallExpressionNode node) {

    }

    @Override
    public void visit(AddressingExpressionNode node) {

    }

    @Override
    public void visit(PostCrementExpressionNode node) {

    }

    @Override
    public void visit(UnaryExpressionNode node) {

    }

    @Override
    public void visit(BinaryExpressionNode node) {

    }

    @Override
    public void visit(AssignExpressionNode node) {

    }

    @Override
    public void visit(ThisPrimaryNode node) {

    }

    @Override
    public void visit(IdentifierPrimaryNode node) {

    }

    @Override
    public void visit(NumericalConstantPrimaryNode node) {

    }

    @Override
    public void visit(BoolConstantPrimaryNode node) {

    }

    @Override
    public void visit(StringConstantPrimaryNode node) {

    }

    @Override
    public void visit(NullConstantPrimaryNode node) {

    }

    @Override
    public void visit(ReturnTypeNode node) {

    }

    @Override
    public void visit(SpecialTypeNode node) {

    }

    @Override
    public void visit(ArrayTypeNode node) {

    }

    @Override
    public void visit(ClassTypeNode node) {

    }

    @Override
    public void visit(BuiltinTypeNode node) {

    }
}

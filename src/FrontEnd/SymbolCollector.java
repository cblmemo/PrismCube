package FrontEnd;

import AST.*;
import AST.DefineNode.*;
import AST.ExpressionNode.*;
import AST.PrimaryNode.*;
import AST.StatementNode.*;
import AST.TypeNode.*;
import Utility.Entity.ConstructorEntity;
import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.Scope.*;
import Utility.Type.ClassType;
import Utility.error.SemanticError;

import static Debug.MemoLog.log;

public class SymbolCollector implements ASTVisitor {
    private GlobalScope globalScope;
    private Scope currentScope;

    public void collect(Memory memory) {
        log.Infof("Symbol collect started.\n");

        globalScope = memory.getGlobalScope();
        visit(memory.getASTRoot());

        log.Infof("Symbol collect finished.\n");
    }

    @Override
    public void visit(ProgramNode node) {
        currentScope = globalScope;
        for (var define : node.getDefines()) {
            define.accept(this);
        }
    }

    @Override
    public void visit(ClassDefineNode node) {
        if (node.isInvalid())
            throw new SemanticError("[collect] error class with error message: " + node.getMessage(), node.getCursor());
        if (currentScope != globalScope)
            throw new SemanticError("[collect] class define not in global scope", node.getCursor());
        if (globalScope.hasFunction(node.getClassName()))
            throw new SemanticError("[collect] class name conflict with existed function", node.getCursor());
        if (globalScope.hasVariable(node.getClassName()))
            throw new SemanticError("[collect] class name conflict with existed variable", node.getCursor());
        currentScope = new ClassScope(globalScope);
        ClassType currentClass = new ClassType(node.getClassName());
        node.getConstructor().accept(this);
        node.getMethods().forEach(function -> function.accept(this));
        currentClass.setClassScope((ClassScope) currentScope);
        node.getMembers().forEach(member -> member.getSingleDefines().forEach(singleDefine -> currentClass.addMember(new VariableEntity(singleDefine.getType().toType(), singleDefine.getVariableName(), singleDefine.getCursor()))));
        currentScope = currentScope.getParentScope();
        globalScope.addClass(node.getClassName(), currentClass);
    }

    @Override
    public void visit(VariableDefineNode node) {

    }

    @Override
    public void visit(SingleVariableDefineNode node) {

    }

    @Override
    public void visit(ConstructorDefineNode node) {
        if (!(currentScope instanceof ClassScope))
            throw new SemanticError("[collect] constructor define outside class scope", node.getCursor());
        ConstructorEntity constructor = new ConstructorEntity(node.getConstructorName(), node.getCursor());
        constructor.setConstructorScope(new ConstructorScope(currentScope));
        ((ClassScope) currentScope).setConstructor(constructor);
    }

    @Override
    public void visit(FunctionDefineNode node) {
        FunctionEntity function = new FunctionEntity(node.getReturnType().toType(), node.getFunctionName(), node.getCursor());
        function.setFunctionScope(new FunctionScope(currentScope));
        node.getParameters().forEach(parameter -> function.addParameter(new VariableEntity(parameter.getType().toType(), parameter.getParameterName(), parameter.getCursor())));
        currentScope.addFunction(function);
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
    public void visit(AtomExpressionNode node) {

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

package FrontEnd;

import AST.*;
import AST.DefineNode.*;
import AST.ExpressionNode.*;
import AST.PrimaryNode.*;
import AST.StatementNode.*;
import AST.TypeNode.*;
import Utility.Scope.*;

public class SemanticChecker implements ASTVisitor {
    private GlobalScope globalScope;
    private Scope currentScope;

    public void check(Memory memory) {
        currentScope = globalScope = memory.getGlobalScope();
        visit(memory.getASTRoot());
    }

    @Override
    public void visit(ProgramNode node) {

    }

    @Override
    public void visit(ClassDefineNode node) {

    }

    @Override
    public void visit(VariableDefineNode node) {

    }

    @Override
    public void visit(SingleVariableDefineNode node) {

    }

    @Override
    public void visit(ConstructorDefineNode node) {

    }

    @Override
    public void visit(FunctionDefineNode node) {

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

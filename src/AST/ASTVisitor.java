package AST;

import AST.DefineNode.*;
import AST.ExpressionNode.*;
import AST.PrimaryNode.*;
import AST.StatementNode.*;
import AST.TypeNode.*;

public interface ASTVisitor {
    void visit(ProgramNode node);

    void visit(ClassDefineNode node);

    void visit(VariableDefineNode node);

    void visit(SingleVariableDefineNode node);

    void visit(ConstructorDefineNode node);

    void visit(FunctionDefineNode node);

    void visit(ParameterDefineNode node);

    void visit(BlockStatementNode node);

    void visit(IfStatementNode node);

    void visit(ForStatementNode node);

    void visit(WhileStatementNode node);

    void visit(ReturnStatementNode node);

    void visit(BreakStatementNode node);

    void visit(ContinueStatementNode node);

    void visit(ExpressionStatementNode node);

    void visit(EmptyStatementNode node);

    void visit(NewTypeExpressionNode node);

    void visit(MemberAccessExpressionNode node);

    void visit(LambdaExpressionNode node);

    void visit(FunctionCallExpressionNode node);

    void visit(AddressingExpressionNode node);

    void visit(PostCrementExpressionNode node);

    void visit(UnaryExpressionNode node);

    void visit(BinaryExpressionNode node);

    void visit(AssignExpressionNode node);

    void visit(ThisPrimaryNode node);

    void visit(IdentifierPrimaryNode node);

    void visit(NumericalConstantPrimaryNode node);

    void visit(BoolConstantPrimaryNode node);

    void visit(StringConstantPrimaryNode node);

    void visit(NullConstantPrimaryNode node);

    void visit(ReturnTypeNode node);

    void visit(SpecialTypeNode node);

    void visit(ArrayTypeNode node);

    void visit(ClassTypeNode node);

    void visit(BuiltinTypeNode node);
}

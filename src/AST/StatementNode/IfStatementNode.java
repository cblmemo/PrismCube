package AST.StatementNode;

import AST.ASTVisitor;
import AST.ExpressionNode.ExpressionNode;
import Utility.Cursor;

public class IfStatementNode extends StatementNode {
    private final ExpressionNode conditionExpression;
    private StatementNode trueStatement, falseStatement;
    private boolean hasFalseStatement = false;

    public IfStatementNode(ExpressionNode conditionExpression, Cursor cursor) {
        super(cursor);
        this.conditionExpression = conditionExpression;
    }

    public void setTrueStatement(StatementNode trueStatement) {
        this.trueStatement = trueStatement;
    }

    public void setFalseStatement(StatementNode falseStatement) {
        this.falseStatement = falseStatement;
        this.hasFalseStatement = true;
    }

    public boolean hasElse() {
        return hasFalseStatement;
    }

    public ExpressionNode getConditionExpression() {
        return conditionExpression;
    }

    public StatementNode getTrueStatement() {
        return trueStatement;
    }

    public StatementNode getFalseStatement() {
        return falseStatement;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

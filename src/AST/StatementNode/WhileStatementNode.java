package AST.StatementNode;

import AST.ASTVisitor;
import AST.ExpressionNode.ExpressionNode;
import Utility.Cursor;

public class WhileStatementNode extends StatementNode {
    private ExpressionNode conditionExpression;
    private StatementNode loopBody;

    public WhileStatementNode(ExpressionNode conditionExpression, StatementNode loopBody, Cursor cursor) {
        super(cursor);
        this.conditionExpression = conditionExpression;
        this.loopBody = loopBody;
    }

    public ExpressionNode getConditionExpression() {
        return conditionExpression;
    }

    public StatementNode getLoopBody() {
        return loopBody;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

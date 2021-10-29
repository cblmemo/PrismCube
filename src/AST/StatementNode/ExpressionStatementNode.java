package AST.StatementNode;

import AST.ASTVisitor;
import AST.ExpressionNode.ExpressionNode;
import Utility.Cursor;

public class ExpressionStatementNode extends StatementNode {
    private ExpressionNode expression;

    public ExpressionStatementNode(ExpressionNode expression, Cursor cursor) {
        super(cursor);
        this.expression = expression;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

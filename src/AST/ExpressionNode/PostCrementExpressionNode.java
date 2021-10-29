package AST.ExpressionNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class PostCrementExpressionNode extends ExpressionNode {
    private ExpressionNode lhs;
    private String op;

    public PostCrementExpressionNode(ExpressionNode lhs, String op, Cursor cursor) {
        super(cursor);
        this.lhs = lhs;
        this.op = op;
    }

    public ExpressionNode getLhs() {
        return lhs;
    }

    public String getOp() {
        return op;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST.ExpressionNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class UnaryExpressionNode extends ExpressionNode {
    private ExpressionNode rhs;
    private String op;

    public UnaryExpressionNode(ExpressionNode rhs, String op, Cursor cursor) {
        super(cursor);
        this.rhs = rhs;
        this.op = op;
    }

    public String getOp() {
        return op;
    }

    public ExpressionNode getRhs() {
        return rhs;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

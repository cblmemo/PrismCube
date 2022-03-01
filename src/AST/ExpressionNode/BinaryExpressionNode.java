package AST.ExpressionNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class BinaryExpressionNode extends ExpressionNode {
    private final ExpressionNode lhs;
    private final ExpressionNode rhs;
    private final String op;

    public BinaryExpressionNode(ExpressionNode lhs, ExpressionNode rhs, String op, Cursor cursor) {
        super(false, cursor);
        this.lhs = lhs;
        this.rhs = rhs;
        this.op = op;
    }

    public ExpressionNode getLhs() {
        return lhs;
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

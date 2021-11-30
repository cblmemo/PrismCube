package AST.ExpressionNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class BinaryExpressionNode extends ExpressionNode {
    private final ExpressionNode lhs;
    private final ExpressionNode rhs;
    private final String op;
    private final String text;

    public BinaryExpressionNode(ExpressionNode lhs, ExpressionNode rhs, String op, String text, Cursor cursor) {
        super(false, cursor);
        this.lhs = lhs;
        this.rhs = rhs;
        this.op = op;
        this.text = text;
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

    public String getText() {
        return text;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

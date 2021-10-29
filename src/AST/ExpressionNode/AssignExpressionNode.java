package AST.ExpressionNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class AssignExpressionNode extends ExpressionNode {
    private ExpressionNode lhs, rhs;
    private String op;

    public AssignExpressionNode(ExpressionNode lhs, ExpressionNode rhs, String op, Cursor cursor) {
        super(cursor);
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

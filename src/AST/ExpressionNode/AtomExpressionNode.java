package AST.ExpressionNode;

import AST.ASTVisitor;
import AST.PrimaryNode.PrimaryNode;
import Utility.Cursor;

public class AtomExpressionNode extends ExpressionNode {
    private PrimaryNode primary;

    public AtomExpressionNode(PrimaryNode primary, Cursor cursor) {
        super(cursor);
        this.primary = primary;
    }

    public PrimaryNode getPrimary() {
        return primary;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

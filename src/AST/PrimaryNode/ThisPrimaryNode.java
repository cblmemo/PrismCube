package AST.PrimaryNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class ThisPrimaryNode extends PrimaryNode {
    public ThisPrimaryNode(Cursor cursor) {
        super(false, cursor);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

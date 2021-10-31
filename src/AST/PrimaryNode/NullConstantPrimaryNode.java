package AST.PrimaryNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class NullConstantPrimaryNode extends PrimaryNode {
    public NullConstantPrimaryNode(Cursor cursor) {
        super(false, cursor);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

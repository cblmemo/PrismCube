package AST.PrimaryNode;

import AST.ExpressionNode.ExpressionNode;
import Utility.Cursor;

public abstract class PrimaryNode extends ExpressionNode {
    public PrimaryNode(Cursor cursor) {
        super(cursor);
    }
}

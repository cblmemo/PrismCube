package AST;

import Utility.Cursor;

abstract public class ASTNode {
    private final Cursor cursor;

    public ASTNode(Cursor cursor) {
        this.cursor = cursor;
    }

    public Cursor getCursor() {
        return cursor;
    }

    abstract public void accept(ASTVisitor visitor);
}

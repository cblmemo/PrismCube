package AST;

import Utility.Cursor;

abstract public class ASTNode {
    public Cursor cur;

    public ASTNode(Cursor cursor) {
        this.cur = cursor;
    }

    abstract public void accept(ASTVisitor visitor);
}

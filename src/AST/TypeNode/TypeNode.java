package AST.TypeNode;

import AST.ASTNode;
import Utility.Cursor;

abstract public class TypeNode extends ASTNode {
    private String typeName;

    public TypeNode(String typeName, Cursor cursor) {
        super(cursor);
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}

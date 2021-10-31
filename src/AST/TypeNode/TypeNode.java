package AST.TypeNode;

import AST.ASTNode;
import Utility.Cursor;
import Utility.Type.ArrayType;
import Utility.Type.ClassType;
import Utility.Type.Type;

abstract public class TypeNode extends ASTNode {
    private String typeName;

    public TypeNode(String typeName, Cursor cursor) {
        super(cursor);
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public Type toType() {
        if (this instanceof ArrayTypeNode)
            return new ArrayType(new ClassType(((ArrayTypeNode) this).getRootTypeName()), ((ArrayTypeNode) this).getDimension());
        return new ClassType(typeName);
    }
}

package AST.TypeNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class ArrayTypeNode extends TypeNode {
    private String rootTypeName;
    private TypeNode elementType;
    private final int dimension;

    public ArrayTypeNode(String typeName, TypeNode elementType, Cursor cursor) {
        super(typeName, cursor);
        this.elementType = elementType;
        if (elementType instanceof ArrayTypeNode) {
            dimension = ((ArrayTypeNode) elementType).getDimension() + 1;
            rootTypeName = ((ArrayTypeNode) elementType).getRootTypeName();
        } else {
            dimension = 1;
            rootTypeName = elementType.getTypeName();
        }
    }

    public String getRootTypeName() {
        return rootTypeName;
    }

    public TypeNode getElementType() {
        return elementType;
    }

    public int getDimension() {
        return dimension;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

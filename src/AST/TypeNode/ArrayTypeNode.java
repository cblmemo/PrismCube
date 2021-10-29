package AST.TypeNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class ArrayTypeNode extends TypeNode {
    private TypeNode elementType;
    private final int dimension;

    public ArrayTypeNode(String typeName, TypeNode elementType, Cursor cursor) {
        super(typeName, cursor);
        this.elementType = elementType;
        if (elementType instanceof ArrayTypeNode) dimension = ((ArrayTypeNode) elementType).getDimension() + 1;
        else dimension = 1;
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

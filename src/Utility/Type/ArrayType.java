package Utility.Type;

public class ArrayType extends Type {
    private ClassType rootElementType;
    private int dimension;

    public ArrayType(ClassType rootElementType, int dimension, String typeName) {
        super(typeName);
        this.rootElementType = rootElementType;
        this.dimension = dimension;
    }
}

package Utility.Type;

public class ArrayType extends Type {
    private final ClassType rootElementType;
    private final int dimension;

    public ArrayType(ClassType rootElementType, int dimension) {
        super(rootElementType.getTypeName() + "[]".repeat(Math.max(0, dimension)));
        this.rootElementType = rootElementType;
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }

    public ClassType getRootElementType() {
        return rootElementType;
    }
}

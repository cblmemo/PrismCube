package Utility.Type;

public class ArrayType extends Type {
    private ClassType rootElementType;
    private int dimension;

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

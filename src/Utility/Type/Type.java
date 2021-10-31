package Utility.Type;

import java.util.Objects;

abstract public class Type {
    private String typeName;

    public Type(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public ClassType getRootType() {
        if (this instanceof ArrayType) return ((ArrayType) this).getRootElementType();
        return (ClassType) this;
    }

    public boolean equal(Type rhs) {
        if (isArrayType()) {
            if (!rhs.isArrayType()) return false;
            return Objects.equals(getRootType().getTypeName(), rhs.getRootType().getTypeName())
                    && ((ArrayType) this).getDimension() == ((ArrayType) rhs).getDimension();
        } else {
            if (rhs.isArrayType()) return false;
            return Objects.equals(getTypeName(), rhs.getTypeName());
        }
    }

    public boolean isArrayType() {
        return this instanceof ArrayType;
    }

    public boolean isInt() {
        return !isArrayType() && Objects.equals(typeName, "int");
    }

    public boolean isBool() {
        return !isArrayType() && Objects.equals(typeName, "bool");
    }

    public boolean isString() {
        return !isArrayType() && Objects.equals(typeName, "string");
    }

    public boolean isVoid() {
        return !isArrayType() && Objects.equals(typeName, "void");
    }

    public boolean isNull() {
        return !isArrayType() && Objects.equals(typeName, "null");
    }
}

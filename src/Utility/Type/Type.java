package Utility.Type;

import IR.IRModule;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

import java.util.Objects;

abstract public class Type {
    private final String typeName;

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

    public boolean isNonBuiltinClassType() {
        return !isArrayType()
                && !Objects.equals(typeName, "int")
                && !Objects.equals(typeName, "bool")
                && !Objects.equals(typeName, "string")
                && !Objects.equals(typeName, "void")
                && !Objects.equals(typeName, "null");
    }

    public boolean isNullAssignable() {
        return isArrayType() || isNonBuiltinClassType();
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

    public IRTypeSystem toIRType(IRModule module) {
        if (this instanceof ArrayType) {
            IRTypeSystem temp = ((ArrayType) this).getRootElementType().toIRType(module);
            for (int i = 0; i < ((ArrayType) this).getDimension(); i++) {
                temp = new IRPointerType(temp);
            }
            return temp;
        }
        if (isInt()) return IRModule.intType;
        if (isBool()) return IRModule.boolType;
        if (isString()) return IRModule.stringType;
        if (isVoid()) return IRModule.voidType;
        // store class with class pointer
        return new IRPointerType(module.getIRType(typeName));
    }
}

package IR.TypeSystem;

import IR.Operand.IRConstNumber;
import IR.Operand.IROperand;

import java.util.Objects;

abstract public class IRTypeSystem {
    abstract public String toString();

    abstract public IROperand getDefaultValue();

    abstract public IRConstNumber getCorrespondingConstOperandType();

    public abstract int sizeof();

    public boolean isBool() {
        if (this instanceof IRIntType)
            return ((IRIntType) this).getBandWidth() == 1;
        return false;
    }

    public boolean isChar() {
        if (this instanceof IRIntType)
            return ((IRIntType) this).getBandWidth() == 8;
        return false;
    }

    public boolean isInt() {
        if (this instanceof IRIntType)
            return ((IRIntType) this).getBandWidth() == 32;
        return false;
    }

    public boolean isString() {
        if (this instanceof IRPointerType)
            return ((IRPointerType) this).getBaseType().isChar();
        return false;
    }

    public boolean isPointer() {
        return this instanceof IRPointerType && !isString();
    }

    public boolean isNull() {
        return this instanceof IRNullType;
    }

    public boolean isArray() {
        return this instanceof IRPointerType && !(((IRPointerType) this).getBaseType() instanceof IRStructureType);
    }

    public boolean isClassPointer() {
        return this instanceof IRPointerType && ((IRPointerType) this).getBaseType() instanceof IRStructureType;
    }

    public boolean isVoid() {
        return this instanceof IRVoidType;
    }

    private boolean isNullAssignable() {
        return this instanceof IRPointerType || this instanceof IRStructureType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof IRTypeSystem)) return false;
        if (isNullAssignable() && ((IRTypeSystem) obj).isNull()) return true;
        if (((IRTypeSystem) obj).isNullAssignable() && isNull()) return true;
        if (this instanceof IRPointerType) {
            if (!(obj instanceof IRPointerType)) return false;
            return Objects.equals(((IRPointerType) this).getBaseType(), ((IRPointerType) obj).getBaseType());
        }
        return Objects.equals(toString(), obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}

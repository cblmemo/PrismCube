package IR.TypeSystem;

import IR.Operand.IROperand;

import java.util.Objects;

abstract public class IRTypeSystem {
    abstract public String toString();

    abstract public IROperand getDefaultValue();

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

    public boolean isVoid() {
        return this instanceof IRVoidType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof IRTypeSystem)) return false;
        return Objects.equals(toString(), obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public boolean isZeroInitializable() {
        // todo update while adding new TypeSystem
        return this instanceof IRArrayType || this instanceof IRStructureType;
    }
}

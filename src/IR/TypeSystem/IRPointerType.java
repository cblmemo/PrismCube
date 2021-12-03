package IR.TypeSystem;

import IR.Operand.IRNull;
import IR.Operand.IROperand;

public class IRPointerType extends IRTypeSystem {
    private final IRTypeSystem baseType;

    public IRPointerType(IRTypeSystem baseType) {
        this.baseType = baseType;
    }

    public static IRTypeSystem constructIRPointerType(IRTypeSystem baseType, int dimension) {
        if (dimension == 0) return baseType;
        return constructIRPointerType(new IRPointerType(baseType), dimension - 1);
    }

    public IRTypeSystem getBaseType() {
        return baseType;
    }

    @Override
    public String toString() {
        return baseType.toString() + "*";
    }

    @Override
    public IROperand getDefaultValue() {
        return new IRNull(this);
    }

    @Override
    public int sizeof() {
        return 4;
    }
}

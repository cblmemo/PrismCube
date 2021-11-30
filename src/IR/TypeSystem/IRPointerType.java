package IR.TypeSystem;

import IR.Operand.IRNull;
import IR.Operand.IROperand;

public class IRPointerType extends IRTypeSystem {
    private final IRTypeSystem baseType;

    public IRPointerType(IRTypeSystem baseType) {
        this.baseType = baseType;
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
}

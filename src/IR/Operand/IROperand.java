package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

abstract public class IROperand {
    private final IRTypeSystem irType;

    public IROperand(IRTypeSystem irType) {
        this.irType = irType;
    }

    public IRTypeSystem getIRType() {
        return irType;
    }

    @Override
    abstract public String toString();
}

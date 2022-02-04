package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

abstract public class IRConstNumber extends IRConst {
    public IRConstNumber(IRTypeSystem irType) {
        super(irType);
    }

    abstract public int getIntValue();

    @Override
    public IROperand toIROperand() {
        return new IRConstInt(null, getIntValue());
    }
}

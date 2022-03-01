package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

abstract public class IRConstNumber extends IRConst {
    public IRConstNumber(IRTypeSystem irType) {
        super(irType);
    }

    abstract public int getIntValue();

    abstract public IRConstNumber cloneFromIntValue(int value);

    @Override
    public IROperand toIROperand() {
        return new IRConstInt(getIntValue());
    }
}

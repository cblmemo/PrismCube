package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

abstract public class IRConstNumber extends IROperand {
    public IRConstNumber(IRTypeSystem irType) {
        super(irType);
    }

    abstract public int getIntValue();
}

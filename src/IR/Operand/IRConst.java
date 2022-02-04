package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

abstract public class IRConst extends IROperand {
    public IRConst(IRTypeSystem irType) {
        super(irType);
    }

    abstract public String toString();

    abstract public IROperand toIROperand();
}

package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

public class IRNull extends IROperand {
    public IRNull(IRTypeSystem irType) {
        super(irType);
    }

    @Override
    public String toString() {
        return "null";
    }
}

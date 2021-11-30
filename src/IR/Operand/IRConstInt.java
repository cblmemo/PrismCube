package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

public class IRConstInt extends IROperand {
    private final int value;

    public IRConstInt(IRTypeSystem irType, int value) {
        super(irType);
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}

package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

public class IRConstChar extends IROperand{
    private final int value;

    public IRConstChar(IRTypeSystem irType, int value) {
        super(irType);
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}

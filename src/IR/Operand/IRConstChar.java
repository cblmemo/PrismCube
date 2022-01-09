package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

public class IRConstChar extends IRConstNumber {
    private final int value;

    public IRConstChar(IRTypeSystem irType, int value) {
        super(irType);
        this.value = value;
    }

    @Override
    public int getIntValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}

package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

public class IRConstInt extends IRConstNumber {
    private final int value;

    public IRConstInt(IRTypeSystem irType, int value) {
        super(irType);
        this.value = value;
    }

    public int getValue() {
        return value;
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

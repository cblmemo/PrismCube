package IR.Operand;

import IR.IRModule;

public class IRConstChar extends IRConstNumber {
    private final int value;

    public IRConstChar(int value) {
        super(IRModule.charType);
        this.value = value;
    }

    @Override
    public int getIntValue() {
        return value;
    }

    @Override
    public IRConstNumber cloneFromIntValue(int value) {
        return new IRConstChar(value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}

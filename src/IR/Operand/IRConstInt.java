package IR.Operand;

import IR.IRModule;

public class IRConstInt extends IRConstNumber {
    private final int value;

    public IRConstInt(int value) {
        super(IRModule.intType);
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
    public IRConstNumber cloneFromIntValue(int value) {
        return new IRConstInt(value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}

package IR.Operand;

import IR.IRModule;

public class IRNull extends IRConstNumber {
    public IRNull() {
        super(IRModule.nullType);
    }

    @Override
    public int getIntValue() {
        return 0;
    }

    @Override
    public IRConstNumber cloneFromIntValue(int value) {
        assert value == 0;
        return new IRNull();
    }

    @Override
    public String toString() {
        return "null";
    }
}

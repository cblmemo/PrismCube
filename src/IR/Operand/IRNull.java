package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

public class IRNull extends IRConstNumber {
    public IRNull(IRTypeSystem irType) {
        super(irType);
    }

    @Override
    public int getIntValue() {
        return 0;
    }

    @Override
    public IRConstNumber cloneFromIntValue(int value) {
        assert value == 0;
        return new IRNull(this.getIRType());
    }

    @Override
    public String toString() {
        return "null";
    }
}

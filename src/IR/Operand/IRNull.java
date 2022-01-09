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
    public String toString() {
        return "null";
    }
}

package IR.TypeSystem;

import IR.Operand.IRConstNumber;
import IR.Operand.IRNull;
import IR.Operand.IROperand;

public class IRNullType extends IRTypeSystem {
    @Override
    public String toString() {
        return "null";
    }

    @Override
    public IROperand getDefaultValue() {
        return null;
    }

    @Override
    public IRConstNumber getCorrespondingConstOperandType() {
        return new IRNull();
    }

    @Override
    public int sizeof() {
        return 0;
    }
}

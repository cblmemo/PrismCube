package IR.TypeSystem;

import IR.Operand.IROperand;

public class IRNullType extends IRTypeSystem{
    @Override
    public String toString() {
        return "null";
    }

    @Override
    public IROperand getDefaultValue() {
        return null;
    }

    @Override
    public int sizeof() {
        return 0;
    }
}

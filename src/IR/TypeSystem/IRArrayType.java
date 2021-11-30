package IR.TypeSystem;

import IR.Operand.IROperand;

public class IRArrayType extends IRTypeSystem {
    private IRTypeSystem elementType;
    private int length;

    @Override
    public String toString() {
        return "[" + length + " x " + elementType.toString() + "]";
    }

    @Override
    public IROperand getDefaultValue() {
        return null;
    }
}

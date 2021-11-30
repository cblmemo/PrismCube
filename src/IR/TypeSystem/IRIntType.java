package IR.TypeSystem;

import IR.Operand.IRConstInt;
import IR.Operand.IROperand;

public class IRIntType extends IRTypeSystem {
    private final int bandWidth;

    public IRIntType(int bandWidth) {
        this.bandWidth = bandWidth;
    }

    public int getBandWidth() {
        return bandWidth;
    }

    @Override
    public String toString() {
        return "i" + bandWidth;
    }

    @Override
    public IROperand getDefaultValue() {
        return new IRConstInt(this, 0);
    }
}

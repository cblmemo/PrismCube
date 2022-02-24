package IR.TypeSystem;

import IR.IRModule;
import IR.Operand.*;

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

    @Override
    public IRConstNumber getCorrespondingConstOperandType() {
        switch (bandWidth) {
            case 1 -> {
                return new IRConstBool(IRModule.boolType, true);
            }
            case 8 -> {
                return new IRConstChar(IRModule.charType, 0);
            }
            case 32 -> {
                return new IRConstInt(IRModule.intType, 0);
            }
            default -> {
                return null;
            }
        }
    }

    @Override
    public int sizeof() {
        return (bandWidth + 7) / 8;
    }
}

package IR.TypeSystem;

import IR.Operand.IROperand;
import Utility.error.IRError;

public class IRVoidType extends IRTypeSystem {
    @Override
    public String toString() {
        return "void";
    }

    @Override
    public IROperand getDefaultValue() {
        throw new IRError("try to get default value of void type (which only appears in function return type)");
    }
}

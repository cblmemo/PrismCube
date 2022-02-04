package ASM.Operand.GlobalSymbol;

import IR.IRGlobalDefine;
import IR.Operand.IRConstNumber;

public class ASMGlobalBoolean extends ASMGlobalSymbol {
    // all global boolean will be initialized by an initialize function
    private final int value;

    public ASMGlobalBoolean(String symbolName, IRGlobalDefine define) {
        super(symbolName);
        assert define.getInitValue() instanceof IRConstNumber : define.getInitValue();
        value = ((IRConstNumber) define.getInitValue()).getIntValue();
    }

    @Override
    public String getValue() {
        return Integer.toUnsignedString(value);
    }

    @Override
    public String getHexValue() {
        return Integer.toUnsignedString(value, 16);
    }
}

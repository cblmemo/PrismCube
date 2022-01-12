package ASM.Operand;

import IR.Operand.IRConstString;

public class ASMConstString extends ASMOperand {
    private final IRConstString value;
    private final String name;

    public ASMConstString(IRConstString value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value.getValue();
    }

    public int getLength() {
        return value.getLength();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}

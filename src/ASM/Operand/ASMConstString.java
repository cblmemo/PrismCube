package ASM.Operand;

import IR.Operand.IRConstString;

public class ASMConstString extends ASMOperand {
    private final String value;
    private final String name;
    private final int length;

    public ASMConstString(IRConstString value, String name) {
        this.value = value.getOriginalValue();
        this.name = name;
        this.length = value.getLength();
    }

    public String getValue() {
        return value;
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}

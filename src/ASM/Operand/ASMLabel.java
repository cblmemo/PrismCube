package ASM.Operand;

public class ASMLabel extends ASMOperand {
    private final String labelName;

    public ASMLabel(String labelName) {
        this.labelName = labelName;
    }

    @Override
    public String toString() {
        return labelName;
    }
}

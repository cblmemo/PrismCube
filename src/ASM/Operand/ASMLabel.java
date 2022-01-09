package ASM.Operand;

public class ASMLabel extends ASMOperand {
    private final String labelName;

    public ASMLabel(String labelName) {
        this.labelName = labelName;
    }

    public String getLabelName() {
        return labelName;
    }

    @Override
    public String toString() {
        return labelName;
    }
}

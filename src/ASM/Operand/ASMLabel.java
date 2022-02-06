package ASM.Operand;

import ASM.ASMBasicBlock;

public class ASMLabel extends ASMOperand {
    private final String labelName;
    private final ASMBasicBlock belongTo; // null represent this label is function label

    public ASMLabel(String labelName, ASMBasicBlock belongTo) {
        this.labelName = labelName;
        this.belongTo = belongTo;
    }

    public ASMBasicBlock belongTo() {
        return belongTo;
    }

    @Override
    public String toString() {
        return labelName;
    }
}

package ASM.Instruction;

import ASM.ASMBasicBlock;

public class ASMCloneInstruction extends ASMInstruction {
    public ASMCloneInstruction(ASMBasicBlock parentBlock, String instStr) {
        super(parentBlock, instStr);
    }

    @Override
    public boolean useless() {
        return false;
    }

    @Override
    public boolean haveImmediateType() {
        return false;
    }
}

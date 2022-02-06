package ASM.Instruction;

import ASM.ASMBasicBlock;
import ASM.Operand.ASMRegister;

public class ASMMoveInstruction extends ASMPseudoInstruction {
    public ASMMoveInstruction(ASMBasicBlock parentBlock, InstType type) {
        super(parentBlock, type);
    }

    public ASMRegister getRd() {
        return (ASMRegister) getOperands().get(0);
    }

    public ASMRegister getRs() {
        return (ASMRegister) getOperands().get(1);
    }

    public boolean eliminable() {
        return getOperands().get(0) == getOperands().get(1);
    }
}

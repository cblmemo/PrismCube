package ASM.Instruction;

import ASM.ASMBasicBlock;
import ASM.Operand.ASMAddress;
import ASM.Operand.ASMRegister;

public class ASMMemoryInstruction extends ASMInstruction {
    public enum InstType {
        lb, lw, sb, sw
    }

    public ASMMemoryInstruction(ASMBasicBlock parentBlock, InstType type, ASMRegister register, ASMAddress address) {
        super(parentBlock, type.toString());
        addOperand(register);
        addOperand(address);
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

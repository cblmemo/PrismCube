package ASM.Instruction;

import ASM.Operand.ASMAddress;
import ASM.Operand.ASMRegister;

public class ASMMemoryInstruction extends ASMInstruction {
    public enum InstType {
        lb, lw, sb, sw
    }

    public ASMMemoryInstruction(InstType type, ASMRegister register, ASMAddress address) {
        super(type.toString());
        addOperand(register);
        addOperand(address);
    }
}

package ASM.Instruction;

import ASM.Operand.ASMRegister;

public class ASMMoveInstruction extends ASMPseudoInstruction {
    public ASMMoveInstruction(InstType type) {
        super(type);
    }

    public ASMRegister getRd() {
        return (ASMRegister) getOperands().get(0);
    }

    public ASMRegister getRs() {
        return (ASMRegister) getOperands().get(1);
    }
}

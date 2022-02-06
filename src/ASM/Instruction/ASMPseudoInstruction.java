package ASM.Instruction;

import ASM.ASMBasicBlock;

public class ASMPseudoInstruction extends ASMInstruction {
    public enum InstType {
        li, mv, call, ret, j, la,
        beqz, seqz, snez;

        public boolean isMove() {
            return this.ordinal() == mv.ordinal();
        }
    }

    public ASMPseudoInstruction(ASMBasicBlock parentBlock, InstType type) {
        super(parentBlock, type.toString());
    }
}

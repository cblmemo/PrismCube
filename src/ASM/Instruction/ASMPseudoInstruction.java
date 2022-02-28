package ASM.Instruction;

import ASM.ASMBasicBlock;

public class ASMPseudoInstruction extends ASMInstruction {
    private final InstType type;

    public enum InstType {
        li, mv, call, ret, j, la,
        beqz, seqz, snez;

        public boolean isMove() {
            return this.ordinal() == mv.ordinal();
        }
    }

    public ASMPseudoInstruction(ASMBasicBlock parentBlock, InstType type) {
        super(parentBlock, type.toString());
        this.type = type;
    }

    @Override
    public boolean useless() {
        if (type == InstType.mv) return getOperands().get(0) == getOperands().get(1);
        return false;
    }
}

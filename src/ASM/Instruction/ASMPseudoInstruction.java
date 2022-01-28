package ASM.Instruction;

import ASM.Operand.ASMRegister;
import ASM.Operand.ASMVirtualRegister;

public class ASMPseudoInstruction extends ASMInstruction {
    public enum InstType {
        li, mv, call, ret, j, la,
        beqz, seqz, snez;

        public boolean isMove() {
            return this.ordinal() == mv.ordinal();
        }
    }

    public ASMPseudoInstruction(InstType type) {
        super(type.toString());
    }

    @Override
    public void replaceRegister(ASMVirtualRegister oldReg, ASMRegister newReg) {
        if (isBranchInstruction()) {
            if (getOperands().get(0) == oldReg) {
                setOperand(0, newReg);
                removeUse(oldReg);
                addUse(newReg);
            }
        } else {
            if (getOperands().get(0) == oldReg) {
                setOperand(0, newReg);
                removeDef(oldReg);
                addDef(newReg);
            }
            for (int i = 1; i < getOperands().size(); i++) {
                if (getOperands().get(i) == oldReg) {
                    setOperand(i, newReg);
                    removeUse(oldReg);
                    addUse(newReg);
                }
            }
        }
    }
}

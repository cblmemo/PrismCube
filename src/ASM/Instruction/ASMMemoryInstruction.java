package ASM.Instruction;

import ASM.Operand.ASMAddress;
import ASM.Operand.ASMRegister;
import ASM.Operand.ASMVirtualRegister;

public class ASMMemoryInstruction extends ASMInstruction {
    public enum InstType {
        lb, lw, sb, sw
    }

    public ASMMemoryInstruction(InstType type, ASMRegister register, ASMAddress address) {
        super(type.toString());
        addOperand(register);
        addOperand(address);
    }

    @Override
    public void replaceRegister(ASMVirtualRegister oldReg, ASMRegister newReg) {
        if (isStoreInstruction()) {
            for (int i = 0; i < getOperands().size(); i++) {
                if (getOperands().get(i) == oldReg) {
                    setOperand(i, newReg);
                    removeUse(oldReg);
                    addUse(newReg);
                }
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

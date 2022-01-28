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
            if (getOperands().get(0) == oldReg) {
                setOperand(0, newReg);
                removeUse(oldReg);
                addUse(newReg);
            }
            if (((ASMAddress) getOperands().get(1)).getRegister() == oldReg) {
                ((ASMAddress) getOperands().get(1)).replaceRegister(newReg);
                removeUse(oldReg);
                addUse(newReg);
            }
        } else {
            if (getOperands().get(0) == oldReg) {
                setOperand(0, newReg);
                removeDef(oldReg);
                addDef(newReg);
            }
            if (((ASMAddress) getOperands().get(1)).getRegister() == oldReg) {
                ((ASMAddress) getOperands().get(1)).replaceRegister(newReg);
                removeUse(oldReg);
                addUse(newReg);
            }
        }
    }
}

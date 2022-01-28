package ASM.Instruction;

import ASM.Operand.ASMRegister;
import ASM.Operand.ASMVirtualRegister;

public class ASMArithmeticInstruction extends ASMInstruction {
    public enum InstType {
        lui, auipc,
        sub,
        add, sll, slt, sltu, xor, srl, sra, or, and,
        addi, slli, slti, sltiu, xori, srli, srai, ori, andi,
        // sdiv -> div, div -> divu
        mul, div, rem;

        public boolean swappable() {
            return ordinal() == add.ordinal() || ordinal() == xor.ordinal() || ordinal() == or.ordinal() || ordinal() == and.ordinal();
        }

        public boolean haveImmediateType() {
            return add.ordinal() <= ordinal() && ordinal() <= and.ordinal();
        }

        public InstType toImmediateType() {
            assert haveImmediateType();
            return values()[ordinal() + 9];
        }
    }

    public ASMArithmeticInstruction(InstType type) {
        super(type.toString());
    }

    @Override
    public void replaceRegister(ASMVirtualRegister oldReg, ASMRegister newReg) {
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

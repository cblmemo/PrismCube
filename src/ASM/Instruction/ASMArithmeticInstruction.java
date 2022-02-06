package ASM.Instruction;

import ASM.ASMBasicBlock;

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

        public boolean isMul() {
            return ordinal() == mul.ordinal();
        }

        public boolean isDiv() {
            return ordinal() == div.ordinal();
        }
    }

    public ASMArithmeticInstruction(ASMBasicBlock parentBlock, InstType type) {
        super(parentBlock, type.toString());
    }
}

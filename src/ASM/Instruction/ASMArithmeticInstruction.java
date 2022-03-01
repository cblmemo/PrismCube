package ASM.Instruction;

import ASM.ASMBasicBlock;
import ASM.Operand.ASMImmediate;

public class ASMArithmeticInstruction extends ASMInstruction {
    private final InstType type;

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

        public boolean isSub() {
            return ordinal() == sub.ordinal();
        }

        public boolean isAdd() {
            return ordinal() == add.ordinal();
        }

        public boolean isXor() {
            return ordinal() == xor.ordinal();
        }
    }

    public ASMArithmeticInstruction(ASMBasicBlock parentBlock, InstType type) {
        super(parentBlock, type.toString());
        this.type = type;
    }

    public ASMArithmeticInstruction(ASMBasicBlock parentBlock, String op) {
        super(parentBlock, op);
        this.type = null;
    }

    public InstType getImmediateType() {
        assert haveImmediateType();
        return type.toImmediateType();
    }

    @Override
    public boolean useless() {
        switch (type) {
            case addi, slli, xori, srli, srai -> {
                assert getOperands().get(2) instanceof ASMImmediate;
                return ((ASMImmediate) getOperands().get(2)).getImm() == 0;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public boolean haveImmediateType() {
        return type.haveImmediateType();
    }
}

package ASM.Instruction;

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
}

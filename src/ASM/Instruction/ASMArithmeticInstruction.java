package ASM.Instruction;

import ASM.ASMVisitor;
import ASM.Instruction.ASMInstruction;
import ASM.Operand.ASMImmediate;
import ASM.Operand.ASMVirtualRegister;

public class ASMArithmeticInstruction extends ASMInstruction {
    public enum InstType {
        lui, auipc,
        add, sub, sll, slt, sltu, xor, srl, sra, or, and,
        addi, slti, sltiu, xori, ori, andi, slli, srli, srai
    }

    private final InstType type;
    private final ASMVirtualRegister rd;
    private final ASMVirtualRegister rs1;
    private final ASMVirtualRegister rs2;
    private final ASMImmediate imm;

    public ASMArithmeticInstruction(InstType type, ASMVirtualRegister rd, ASMVirtualRegister rs1, ASMVirtualRegister rs2, ASMImmediate imm) {
        this.type = type;
        this.rd = rd;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.imm = imm;
    }

    private String getInstStr() {
        return ASMInstruction.align(type.toString());
    }

    @Override
    public String toString() {
        if (type.ordinal() <= InstType.auipc.ordinal()) return getInstStr() + rd + ", " + imm;
        if (type.ordinal() <= InstType.and.ordinal()) return getInstStr() + rd + ", " + rs1 + ", " + rs2;
        return getInstStr() + rd + ", " + rs1 + ", " + imm;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}

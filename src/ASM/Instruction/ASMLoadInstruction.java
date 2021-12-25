package ASM.Instruction;

import ASM.ASMVisitor;
import ASM.Operand.ASMImmediate;
import ASM.Operand.ASMVirtualRegister;

public class ASMLoadInstruction extends ASMInstruction {
    public enum InstType {
        lb, lw
    }

    private InstType type;
    private ASMVirtualRegister loadTarget;
    private ASMVirtualRegister loadSource;
    private ASMImmediate offset;

    public ASMLoadInstruction(InstType type, ASMVirtualRegister loadTarget, ASMVirtualRegister loadSource, ASMImmediate offset) {
        this.type = type;
        this.loadTarget = loadTarget;
        this.loadSource = loadSource;
        this.offset = offset;
    }

    private String getInstStr() {
        return ASMInstruction.align(type.toString());
    }

    private String getAddress() {
        // offset == null -> no offset
        if (offset == null) return loadSource.toString();
        return offset + "(" + loadSource + ")";
    }

    @Override
    public String toString() {
        return getInstStr() + loadTarget + ", " + getAddress();
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}

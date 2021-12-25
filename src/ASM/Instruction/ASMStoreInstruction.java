package ASM.Instruction;

import ASM.ASMVisitor;
import ASM.Operand.ASMImmediate;
import ASM.Operand.ASMOperand;
import ASM.Operand.ASMVirtualRegister;

public class ASMStoreInstruction extends ASMInstruction{
    public enum InstType {
        sb, sw
    }

    private InstType type;
    private ASMVirtualRegister storeValue;
    private ASMVirtualRegister storeTarget;
    private ASMImmediate offset;

    public ASMStoreInstruction(InstType type, ASMVirtualRegister storeValue, ASMVirtualRegister storeTarget, ASMImmediate offset) {
        this.type = type;
        this.storeValue = storeValue;
        this.storeTarget = storeTarget;
        this.offset = offset;
    }

    private String getInstStr() {
        return ASMInstruction.align(type.toString());
    }

    private String getAddress() {
        // offset == null -> no offset
        if (offset == null) return storeTarget.toString();
        return offset + "(" + storeTarget + ")";
    }

    @Override
    public String toString() {
        return getInstStr() + storeValue + ", " + getAddress();
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}

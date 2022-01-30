package ASM.Operand;

import ASM.ASMStackFrame;

public class ASMAddress extends ASMOperand {
    private ASMRegister register;
    private final ASMImmediate offset;

    private boolean needAddFrameSize = false;
    private ASMStackFrame stackFrame;

    public ASMAddress(ASMRegister register, ASMImmediate offset) {
        this.register = register;
        this.offset = offset;
    }

    public void markAsNeedAddFrameSize(ASMStackFrame stackFrame) {
        needAddFrameSize = true;
        this.stackFrame = stackFrame;
    }

    public ASMRegister getRegister() {
        return register;
    }

    public void replaceRegister(ASMRegister register) {
        this.register = register;
    }

    @Override
    public String toString() {
        if (offset == null) return 0 + "(" + register.toString() + ")";
        int trueOffset = offset.getImm();
        if (needAddFrameSize) trueOffset += stackFrame.getFrameSize();
        return trueOffset + "(" + register + ")";
    }
}

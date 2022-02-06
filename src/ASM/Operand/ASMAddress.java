package ASM.Operand;

import ASM.ASMStackFrame;

import java.util.Objects;

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

    private int getTrueOffset() {
        if (needAddFrameSize) return offset.getImm() + stackFrame.getFrameSize();
        return offset.getImm();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASMAddress that = (ASMAddress) o;
        return Objects.equals(register, that.register) && getTrueOffset() == that.getTrueOffset();
    }

    @Override
    public String toString() {
        if (offset == null) return 0 + "(" + register.toString() + ")";
        int trueOffset = getTrueOffset();
        return trueOffset + "(" + register + ")";
    }
}

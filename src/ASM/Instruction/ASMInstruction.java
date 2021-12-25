package ASM.Instruction;

import ASM.ASMVisitor;

abstract public class ASMInstruction {
    static int alignLength = 10;

    protected static String align(String inst) {
        return inst + " ".repeat(alignLength - inst.length());
    }

    abstract public String toString();

    abstract public void accept(ASMVisitor visitor);
}

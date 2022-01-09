package ASM.Instruction;

public class ASMPseudoInstruction extends ASMInstruction {
    public enum InstType {
        li, mv, call, ret, j,
        beqz, seqz, snez
    }

    public ASMPseudoInstruction(InstType type) {
        super(type.toString());
    }
}

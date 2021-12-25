package ASM;

import ASM.Instruction.ASMInstruction;

import java.util.ArrayList;

public class ASMBasicBlock {
    private final String label;
    private final ASMFunction parentFunction;
    private final ArrayList<ASMInstruction> instructions = new ArrayList<>();

    public ASMBasicBlock(ASMFunction parentFunction, String label) {
        this.parentFunction = parentFunction;
        this.label = label;
    }

    public void appendInstruction(ASMInstruction inst) {
        instructions.add(inst);
    }

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}

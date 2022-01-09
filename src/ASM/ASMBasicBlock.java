package ASM;

import ASM.Instruction.ASMInstruction;
import ASM.Operand.ASMLabel;

import java.util.ArrayList;

public class ASMBasicBlock {
    private final ASMLabel label;
    private ArrayList<ASMInstruction> instructions = new ArrayList<>();

    private static int cnt = 0;

    public ASMBasicBlock(ASMFunction parentFunction, String label) {
        this.label = new ASMLabel(".LBB_" + parentFunction.getFunctionName() + "_" + label + "_" + (++cnt));
    }

    public void appendInstruction(ASMInstruction inst) {
        instructions.add(inst);
    }

    public ASMLabel getLabel() {
        return label;
    }

    public ArrayList<ASMInstruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(ArrayList<ASMInstruction> instructions) {
        this.instructions = instructions;
    }
}

package ASM;

import ASM.Instruction.ASMInstruction;
import ASM.Operand.ASMLabel;
import ASM.Operand.ASMRegister;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class ASMBasicBlock {
    private static final boolean printLabelName = false;
    private final ASMLabel label;
    private ArrayList<ASMInstruction> instructions = new ArrayList<>();
    private final ArrayList<ASMBasicBlock> predecessors = new ArrayList<>();
    private final ArrayList<ASMBasicBlock> successors = new ArrayList<>();
    private LinkedHashSet<ASMRegister> liveOut;

    private static int cnt = 0;
    private static int functionCnt = 0;
    private static final LinkedHashMap<ASMFunction, Integer> functionId = new LinkedHashMap<>();
    private static final LinkedHashMap<Integer, Integer> functionLabelCnt = new LinkedHashMap<>();

    public ASMBasicBlock(ASMFunction parentFunction, String label) {
        String labelName;
        if (printLabelName) labelName = ".LBB_" + parentFunction.getFunctionName() + "_" + label + "_" + (++cnt);
        else {
            if (!functionId.containsKey(parentFunction)) {
                functionId.put(parentFunction, functionCnt);
                functionLabelCnt.put(functionCnt++, 0);
            }
            int funcId = functionId.get(parentFunction);
            int blockId = functionLabelCnt.get(funcId);
            labelName = ".LBB_" + functionId.get(parentFunction) + "_" + blockId;
            functionLabelCnt.put(funcId, blockId + 1);
        }
        this.label = new ASMLabel(labelName);
    }

    public void appendInstruction(ASMInstruction inst) {
        instructions.add(inst);
    }

    public void addPredecessor(ASMBasicBlock pred) {
        predecessors.add(pred);
    }

    public void addSuccessor(ASMBasicBlock pred) {
        successors.add(pred);
    }

    public ArrayList<ASMBasicBlock> getPredecessors() {
        return predecessors;
    }

    public ArrayList<ASMBasicBlock> getSuccessors() {
        return successors;
    }

    public void setLiveOut(LinkedHashSet<ASMRegister> liveOut) {
        this.liveOut = liveOut;
    }

    public LinkedHashSet<ASMRegister> getLiveOut() {
        return liveOut;
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

    @Override
    public String toString() {
        return label.toString();
    }
}

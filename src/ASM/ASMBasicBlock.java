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
    private final int loopDepth;

    private static int cnt = 0;
    private static int functionCnt = 0;
    private static final LinkedHashMap<ASMFunction, Integer> functionId = new LinkedHashMap<>();
    private static final LinkedHashMap<Integer, Integer> functionLabelCnt = new LinkedHashMap<>();

    public ASMBasicBlock(ASMFunction parentFunction, String label, int loopDepth) {
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
        this.label = new ASMLabel(labelName, this);
        this.loopDepth = loopDepth;
    }

    public void appendInstruction(ASMInstruction inst) {
        instructions.add(inst);
    }

    public void replaceInstruction(ASMInstruction oldInst, ASMInstruction newInst) {
        instructions.set(instructions.indexOf(oldInst), newInst);
    }

    public void addPredecessor(ASMBasicBlock pred) {
        if (!predecessors.contains(pred)) predecessors.add(pred);
    }

    public void addSuccessor(ASMBasicBlock succ) {
        if (!successors.contains(succ)) successors.add(succ);
    }

    public void removePredecessor(ASMBasicBlock pred) {
        predecessors.remove(pred);
    }

    public void removeSuccessor(ASMBasicBlock succ) {
        successors.remove(succ);
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

    public boolean isDirectlyJumpBlock() {
        return instructions.size() == 1 && instructions.get(0).isJump();
    }

    public ASMBasicBlock getDirectlyJumpTarget() {
        assert isDirectlyJumpBlock();
        return ((ASMLabel) instructions.get(0).getOperands().get(0)).belongTo();
    }

    public String getPredecessorsStr() {
        if (predecessors.isEmpty()) return "[ ]";
        StringBuilder builder = new StringBuilder("[ ").append(predecessors.get(0));
        for (int i = 1; i < predecessors.size(); i++) builder.append(", ").append(predecessors.get(i));
        builder.append(" ]");
        return builder.toString();
    }

    public boolean withJumpEscapeInstruction() {
        assert instructions.size() > 0 : this;
        if (instructions.get(instructions.size() - 1).isRet()) return false;
        if (instructions.size() == 1) {
            assert instructions.get(0).isJump() : instructions.get(0);
            return true;
        }
        assert instructions.get(instructions.size() - 1).isJump() : instructions.get(instructions.size() - 1);
        return !instructions.get(instructions.size() - 2).isBranch();
    }

    public boolean endWithJump() {
        return instructions.get(instructions.size() - 1).isJump();
    }

    public boolean isPullableReturnBlock() {
        return instructions.get(instructions.size() - 1).isRet();
    }

    public ASMBasicBlock getJumpTarget() {
        if (!endWithJump()) return null;
        return ((ASMLabel) instructions.get(instructions.size() - 1).getOperands().get(0)).belongTo();
    }

    public ASMBasicBlock getTailBranchTarget() {
        if (!endWithJump()) return null;
        if (instructions.size() <= 1) return null;
        if (instructions.get(instructions.size() - 2).isBranch())
            return ((ASMLabel) instructions.get(instructions.size() - 2).getOperands().get(1)).belongTo();
        return null;
    }

    // beqz reg, L1   ->   bnez reg, L2
    // j    L2        j    L1
    public void swapTailBranch() {
        assert getTailBranchTarget() != null;
        ASMInstruction brInst = instructions.get(instructions.size() - 2), jInst = instructions.get(instructions.size() - 1);
        assert brInst.getOperands().get(1) instanceof ASMLabel && jInst.getOperands().get(0) instanceof ASMLabel;
        ASMLabel brLabel = (ASMLabel) brInst.getOperands().get(1);
        brInst.getOperands().set(1, jInst.getOperands().get(0));
        jInst.getOperands().set(0, brLabel);
        brInst.swapBranch();
    }

    public void removeTailJump() {
        assert endWithJump();
        instructions.remove(instructions.size() - 1);
    }

    public void replacePredecessor(ASMBasicBlock oldBlock, ASMBasicBlock newBlock) {
        assert predecessors.contains(oldBlock) : this + " " + predecessors + " " + oldBlock + " " + newBlock;
        predecessors.remove(oldBlock);
        if (!predecessors.contains(newBlock)) predecessors.add(newBlock);
    }

    public void fuse(ASMBasicBlock successor) {
        assert withJumpEscapeInstruction();
        instructions.remove(instructions.size() - 1);
        instructions.addAll(successor.getInstructions());
        successor.getInstructions().forEach(inst -> inst.setParentBlock(this));
        successors.clear();
        successor.getSuccessors().forEach(succSucc -> {
            if (!successors.contains(succSucc)) successors.add(succSucc);
            succSucc.replacePredecessor(successor, this);
        });
    }

    public int getLoopDepth() {
        return loopDepth;
    }

    public boolean definedBeforeUse(ASMRegister reg) {
        for (ASMInstruction inst : instructions) {
            if (inst.isCall() || inst.getUses().contains(reg)) return false;
            if (inst.getDefs().contains(reg)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return label.toString();
    }
}

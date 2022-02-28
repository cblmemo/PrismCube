package IR;

import FrontEnd.IRVisitor;
import IR.Instruction.*;
import IR.Operand.IRLabel;
import MiddleEnd.Utils.CopyInterfereGraph;

import java.util.ArrayList;
import java.util.function.Consumer;

public class IRBasicBlock {
    private final IRFunction parentFunction;
    private final IRLabel label;
    private final String labelName;
    private final ArrayList<IRInstruction> instructions = new ArrayList<>();
    private ArrayList<IRAllocaInstruction> allocas;
    private final ArrayList<IRPhiInstruction> phis = new ArrayList<>();
    private IRInstruction escapeInstruction = null;
    private boolean isEntryBlock = false;
    private boolean isReturnBlock = false;
    private final ArrayList<IRBasicBlock> predecessors = new ArrayList<>();
    private final ArrayList<IRBasicBlock> successors = new ArrayList<>();
    private final ArrayList<IRBasicBlock> dominatorTreeSuccessors = new ArrayList<>();
    private IRBasicBlock idom = null;
    private IRBasicBlock postIdom = null;
    private int loopDepth = 0;
    private final CopyInterfereGraph graph = new CopyInterfereGraph();

    private boolean hasFinished = false;

    private static final String prefix = "LABEL$";
    private static final String delim = ".";

    public IRBasicBlock(IRFunction parentFunction, String labelName) {
        this.parentFunction = parentFunction;
        this.label = new IRLabel(prefix + parentFunction.getFunctionName() + delim + labelName, this);
        this.labelName = labelName;
    }

    public IRFunction getParentFunction() {
        return parentFunction;
    }

    public IRLabel getLabel() {
        return label;
    }

    public String getLabelName() {
        return labelName;
    }

    public String getLabelWithFunctionName() {
        return parentFunction.getFunctionName() + "_" + labelName;
    }

    public void appendInstruction(IRInstruction inst) {
        assert !hasFinished;
        instructions.add(inst);
    }

    public void appendInstructionWithoutCheck(IRInstruction inst) {
        instructions.add(inst);
    }

    public void appendAlloca(IRAllocaInstruction inst) {
        allocas.add(inst);
    }

    public void setEscapeInstruction(IRInstruction escapeInstruction) {
        assert this.escapeInstruction == null;
        assert escapeInstruction instanceof IRReturnInstruction || escapeInstruction instanceof IRBrInstruction || escapeInstruction instanceof IRJumpInstruction;
        this.escapeInstruction = escapeInstruction;
    }

    public void setEscapeInstructionWithoutCheck(IRInstruction escapeInstruction) {
        this.escapeInstruction = escapeInstruction;
    }

    public boolean hasEscapeInstruction() {
        return this.escapeInstruction != null;
    }

    public void finishBlock() {
        assert !hasFinished;
        assert escapeInstruction != null : this;
        hasFinished = true;
        instructions.add(escapeInstruction);
    }

    public IRInstruction getEscapeInstruction() {
        return escapeInstruction;
    }

    public void removeFromParentFunction() {
        parentFunction.getBlocks().remove(this);
        predecessors.forEach(pred -> removeSuccessor(this));
        successors.forEach(succ -> removePredecessor(this));
    }

    public void fuse(IRBasicBlock successor) {
        assert hasFinished;
        instructions.remove(instructions.get(instructions.size() - 1));
        instructions.addAll(successor.getInstructions());
        successor.getInstructions().forEach(inst -> inst.setParentBlock(this));
        this.escapeInstruction = successor.getEscapeInstruction();
        if (successor.getAllocas() != null) allocas.addAll(successor.getAllocas());
        phis.addAll(successor.getPhis());
        ArrayList<IRInstruction> users = new ArrayList<>(successor.getLabel().getUsers());
        users.forEach(user -> {
            if (user instanceof IRPhiInstruction) ((IRPhiInstruction) user).replaceSourceBlock(successor, this);
        });
        if (successor.isReturnBlock()) isReturnBlock = true;
        successors.clear();
        successor.getSuccessors().forEach(succSucc -> {
            if (!successors.contains(succSucc)) successors.add(succSucc);
            succSucc.replacePredecessor(successor, this);
        });
    }

    public void replaceInstructions(IRInstruction oldInst, IRInstruction newInst) {
        assert instructions.contains(oldInst);
        int index = instructions.indexOf(oldInst);
        instructions.add(index, newInst);
        oldInst.removeFromParentBlock();
        if (escapeInstruction == oldInst) escapeInstruction = newInst;
    }

    public void replaceControlFlowTarget(IRBasicBlock oldBlock, IRBasicBlock newBlock) {
        if (escapeInstruction instanceof IRBrInstruction) ((IRBrInstruction) escapeInstruction).replaceControlFlowTarget(oldBlock, newBlock);
        else if (escapeInstruction instanceof IRJumpInstruction) ((IRJumpInstruction) escapeInstruction).replaceControlFlowTarget(oldBlock, newBlock);
        else assert false : "replaceControlFlowTarget for return block";
        successors.remove(oldBlock);
        successors.add(newBlock);
        newBlock.addPredecessor(this);
    }

    public void insertInstructionBeforeEscape(IRInstruction inst) {
        assert instructions.get(instructions.size() - 1) == escapeInstruction;
        instructions.add(instructions.size() - 1, inst);
    }

    public void insertPhiInstruction(IRPhiInstruction phi) {
        instructions.add(phis.size(), phi);
        phis.add(phi);
    }

    public void addPhi(IRPhiInstruction phi) {
        phis.add(phi);
    }

    public ArrayList<IRPhiInstruction> getPhis() {
        return phis;
    }

    public ArrayList<IRInstruction> getInstructions() {
        return instructions;
    }

    public void forEachInstruction(Consumer<IRInstruction> consumer) {
        instructions.forEach(consumer);
    }

    public ArrayList<IRAllocaInstruction> getAllocas() {
        return allocas;
    }

    public void markReturnBlock(boolean isReturnBlock) {
        this.isReturnBlock = isReturnBlock;
    }

    public boolean isReturnBlock() {
        return isReturnBlock;
    }

    public void markAsEntryBlock() {
        isEntryBlock = true;
        allocas = new ArrayList<>();
    }

    public boolean isEntryBlock() {
        return isEntryBlock;
    }

    public boolean isEmpty() {
        return instructions.isEmpty() && escapeInstruction == null;
    }

    public void addDominatorTreeSuccessor(IRBasicBlock successor) {
        dominatorTreeSuccessors.add(successor);
    }

    public ArrayList<IRBasicBlock> getDominatorTreeSuccessors() {
        return dominatorTreeSuccessors;
    }

    public void addPredecessor(IRBasicBlock predecessor) {
        if (!predecessors.contains(predecessor)) {
            predecessors.add(predecessor);
            predecessor.addSuccessor(this);
        }
    }

    public void removePredecessor(IRBasicBlock predecessor) {
        assert predecessors.contains(predecessor);
        predecessors.remove(predecessor);
    }

    public ArrayList<IRBasicBlock> getPredecessors() {
        return predecessors;
    }

    public void replacePredecessor(IRBasicBlock oldBlock, IRBasicBlock newBlock) {
        assert predecessors.contains(oldBlock);
        predecessors.remove(oldBlock);
        if (!predecessors.contains(newBlock)) predecessors.add(newBlock);
    }

    private void addSuccessor(IRBasicBlock successor) {
        successors.add(successor);
    }

    public void removeSuccessor(IRBasicBlock successor) {
        assert successors.contains(successor);
        successors.remove(successor);
    }

    public ArrayList<IRBasicBlock> getSuccessors() {
        return successors;
    }

    public String getPreds() {
        if (predecessors.isEmpty()) return "";
        StringBuilder builder = new StringBuilder("; preds = ");
        for (int i = 0; i < predecessors.size(); i++) {
            if (i != 0) builder.append(", ");
            builder.append(predecessors.get(i).getLabel());
        }
        return builder.toString();
    }

    public void setIdom(IRBasicBlock idom) {
        this.idom = idom;
    }

    public void setPostIdom(IRBasicBlock postIdom) {
        this.postIdom = postIdom;
    }

    public IRBasicBlock getPostIdom() {
        return postIdom;
    }

    public boolean dominatedBy(IRBasicBlock suspiciousAncestor) {
        if (idom == suspiciousAncestor) return true;
        if (idom == null) return false;
        return idom.dominatedBy(suspiciousAncestor);
    }

    public CopyInterfereGraph getGraph() {
        return graph;
    }

    public void setLoopDepth(int loopDepth) {
        this.loopDepth = loopDepth;
    }

    public int getLoopDepth() {
        return loopDepth;
    }

    @Override
    public String toString() {
        return label.toString();
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

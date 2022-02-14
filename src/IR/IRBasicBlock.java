package IR;

import FrontEnd.IRVisitor;
import IR.Instruction.*;
import IR.Operand.IRLabel;

import java.util.ArrayList;

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

    private boolean hasFinished = false;

    private static final String prefix = "LABEL$";
    private static final String delim = ".";

    public IRBasicBlock(IRFunction parentFunction, String labelName) {
        this.parentFunction = parentFunction;
        this.label = new IRLabel(prefix + parentFunction.getFunctionName() + delim + labelName);
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

    public void appendInstruction(IRInstruction inst) {
        assert !hasFinished;
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

    public boolean hasEscapeInstruction() {
        return this.escapeInstruction != null;
    }

    public void finishBlock() {
        assert !hasFinished;
        assert escapeInstruction != null;
        hasFinished = true;
        instructions.add(escapeInstruction);
    }

    public IRInstruction getEscapeInstruction() {
        return escapeInstruction;
    }

    public void fuse(IRBasicBlock successor) {
        assert hasFinished;
        instructions.remove(instructions.get(instructions.size() - 1));
        instructions.addAll(successor.getInstructions());
        successor.getInstructions().forEach(inst -> inst.setParentBlock(this));
        this.escapeInstruction = successor.getEscapeInstruction();
        if (successor.getAllocas() != null) allocas.addAll(successor.getAllocas());
        phis.addAll(successor.getPhis());
        successor.getLabel().getUsers().forEach(user -> {
            if (user instanceof IRPhiInstruction) ((IRPhiInstruction) user).replaceSourceBlock(successor, this);
        });
        if (successor.isReturnBlock()) isReturnBlock = true;
        successors.clear();
        successor.getSuccessors().forEach(succSucc -> {
            if (!successors.contains(succSucc)) successors.add(succSucc);
            succSucc.replacePredecessor(successor, this);
        });
    }

    public void replaceControlFlowTarget(IRBasicBlock oldBlock, IRBasicBlock newBlock) {
        if (escapeInstruction instanceof IRBrInstruction) ((IRBrInstruction) escapeInstruction).replaceControlFlowTarget(oldBlock, newBlock);
        else if (escapeInstruction instanceof IRJumpInstruction) ((IRJumpInstruction) escapeInstruction).replaceControlFlowTarget(oldBlock, newBlock);
        else assert false : "replaceControlFlowTarget for return block";
        successors.remove(oldBlock);
        successors.add(newBlock);
    }

    public void insertInstructionBeforeEscape(IRInstruction inst) {
        assert instructions.get(instructions.size() - 1) == escapeInstruction;
        instructions.remove(escapeInstruction);
        instructions.add(inst);
        instructions.add(escapeInstruction);
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

    public ArrayList<IRAllocaInstruction> getAllocas() {
        return allocas;
    }

    public void markAsReturnBlock() {
        isReturnBlock = true;
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
        predecessors.add(predecessor);
        predecessor.addSuccessor(this);
    }

    public void removePredecessor(IRBasicBlock predecessor) {
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

    @Override
    public String toString() {
        return label.toString();
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

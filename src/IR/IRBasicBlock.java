package IR;

import IR.Instruction.IRInstruction;
import IR.Operand.IRLabel;

import java.util.ArrayList;

public class IRBasicBlock {
    private final IRLabel label;
    private final ArrayList<IRInstruction> instructions = new ArrayList<>();
    private IRInstruction escapeInstruction = null;
    private boolean isReturnBlock = false;
    private final ArrayList<IRBasicBlock> predecessors = new ArrayList<>();

    private boolean hasFinished = false;

    private static final String prefix = "LABEL$";
    private static final String delim = ".";

    public IRBasicBlock(IRFunction parentFunction, String labelName) {
        this.label = new IRLabel(prefix + parentFunction.getFunctionName() + delim + labelName);
    }

    public IRLabel getLabel() {
        return label;
    }

    public void appendInstruction(IRInstruction inst) {
        assert !hasFinished;
        instructions.add(inst);
    }

    public void setEscapeInstruction(IRInstruction escapeInstruction) {
        assert this.escapeInstruction == null;
        this.escapeInstruction = escapeInstruction;
    }

    public boolean hasEscapeInstruction() {
        return this.escapeInstruction != null;
    }

    public void finishBlock() {
        assert !hasFinished;
        assert escapeInstruction != null;
        instructions.add(escapeInstruction);
        hasFinished = true;
    }

    public ArrayList<IRInstruction> getInstructions() {
        return instructions;
    }

    public void markAsReturnBlock() {
        isReturnBlock = true;
    }

    public boolean isReturnBlock() {
        return isReturnBlock;
    }

    public void addPredecessor(IRBasicBlock predecessor) {
        predecessors.add(predecessor);
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

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

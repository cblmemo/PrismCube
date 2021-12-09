package IR;

import IR.Instruction.IRAllocaInstruction;
import IR.Instruction.IRBrInstruction;
import IR.Instruction.IRInstruction;
import IR.Instruction.IRReturnInstruction;
import IR.Operand.IRLabel;

import java.util.ArrayList;

public class IRBasicBlock {
    private final IRLabel label;
    private final ArrayList<IRInstruction> instructions = new ArrayList<>();
    private ArrayList<IRAllocaInstruction> allocas;
    private IRInstruction escapeInstruction = null;
    private boolean isEntryBlock = false;
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

    public void appendAlloca(IRAllocaInstruction inst) {
        allocas.add(inst);
    }

    public void setEscapeInstruction(IRInstruction escapeInstruction) {
        assert this.escapeInstruction == null;
        assert escapeInstruction instanceof IRReturnInstruction || escapeInstruction instanceof IRBrInstruction;
        this.escapeInstruction = escapeInstruction;
    }

    public boolean hasEscapeInstruction() {
        return this.escapeInstruction != null;
    }

    public void finishBlock() {
        assert !hasFinished;
        assert escapeInstruction != null;
        hasFinished = true;
    }

    public ArrayList<IRInstruction> getInstructions() {
        return instructions;
    }

    public ArrayList<IRAllocaInstruction> getAllocas() {
        return allocas;
    }

    public IRInstruction getEscapeInstruction() {
        return escapeInstruction;
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

package IR;

import IR.Instruction.IRInstruction;
import IR.Operand.IRLabel;

import java.util.ArrayList;

public class IRBasicBlock {
    private final IRFunction parentFunction;
    private final IRLabel label;
    private final ArrayList<IRInstruction> instructions = new ArrayList<>();
    private IRInstruction escapeInstruction;
    private boolean isReturnBlock = false;

    private static final String prefix = "_";
    private static final String delim = "$";

    public IRBasicBlock(IRFunction parentFunction, String labelName) {
        this.parentFunction = parentFunction;
        this.label = new IRLabel(prefix + labelName + delim + parentFunction.getFunctionName());
    }

    public IRLabel getLabel() {
        return label;
    }

    public void appendInstruction(IRInstruction inst) {
        instructions.add(inst);
    }

    public void setEscapeInstruction(IRInstruction escapeInstruction) {
        this.escapeInstruction = escapeInstruction;
    }

    public void finishBlock() {
        instructions.add(escapeInstruction);
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

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

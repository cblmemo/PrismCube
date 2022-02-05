package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;

abstract public class IRInstruction {
    private static final boolean useAlign = true;

    private String comment = null;
    private final IRBasicBlock parentBlock;

    public IRInstruction(IRBasicBlock parentBlock) {
        this.parentBlock = parentBlock;
    }

    public IRBasicBlock getParentBlock() {
        return parentBlock;
    }

    public void removeFromParentBlock() {
        parentBlock.getInstructions().remove(this);
    }

    abstract public void replaceUse(IROperand oldOperand, IROperand newOperand);

    public static boolean useAlign() {
        return useAlign;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment == null ? "" : "; " + comment;
    }

    abstract public String toString();

    abstract public void accept(IRVisitor visitor);
}

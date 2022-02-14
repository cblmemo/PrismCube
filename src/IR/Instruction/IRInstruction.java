package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;

import java.util.LinkedHashSet;

abstract public class IRInstruction {
    private static final boolean useAlign = true;

    private String comment = null;
    private IRBasicBlock parentBlock;
    private final LinkedHashSet<IROperand> users = new LinkedHashSet<>();

    public IRInstruction(IRBasicBlock parentBlock) {
        this.parentBlock = parentBlock;
    }

    public IRBasicBlock getParentBlock() {
        return parentBlock;
    }

    public void setParentBlock(IRBasicBlock parentBlock) {
        this.parentBlock = parentBlock;
    }

    public void removeFromParentBlock() {
        parentBlock.getInstructions().remove(this);
        if (this instanceof IRAllocaInstruction) {
            assert parentBlock.getAllocas() != null : this + "'s parent block " + parentBlock + " has no allocas";
            parentBlock.getAllocas().remove(this);
        }
        if (this instanceof IRPhiInstruction) parentBlock.getPhis().remove(this);
        users.forEach(user -> user.removeUser(this));
    }

    public void addUser(IROperand operand) {
        users.add(operand);
    }

    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        if (users.contains(oldOperand)) {
            users.remove(oldOperand);
            users.add(newOperand);
        }
    }

    public static boolean useAlign() {
        return useAlign;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment == null ? "" : "; " + comment;
    }

    abstract public boolean noUsersAndSafeToRemove();

    abstract public String toString();

    abstract public void accept(IRVisitor visitor);
}

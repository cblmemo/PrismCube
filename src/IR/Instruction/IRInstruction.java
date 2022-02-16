package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;

import java.util.LinkedHashSet;

abstract public class IRInstruction {
    private static final boolean useAlign = true;

    private String comment = null;
    private IRBasicBlock parentBlock;
    private final LinkedHashSet<IROperand> uses = new LinkedHashSet<>();

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
        uses.forEach(user -> user.removeUser(this));
    }

    public void addUse(IROperand operand) {
        uses.add(operand);
    }

    public void removeUse(IROperand operand) {
        uses.remove(operand);
    }

    public LinkedHashSet<IROperand> getUses() {
        return uses;
    }

    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        if (uses.contains(oldOperand)) {
            uses.remove(oldOperand);
            uses.add(newOperand);
            oldOperand.removeUser(this);
            newOperand.addUser(this);
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

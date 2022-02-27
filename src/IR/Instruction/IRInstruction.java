package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import MiddleEnd.Utils.CloneManager;
import Utility.error.OptimizeError;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

abstract public class IRInstruction {
    private static final boolean useAlign = true;

    private static final LinkedHashSet<IRInstruction> removed = new LinkedHashSet<>();

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
        else removed.add(this);
        uses.forEach(usedOperand -> usedOperand.removeUser(this));
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

    public void replaceAllUseWithValue(IROperand value) {
        assert getDef() != null;
        LinkedHashSet<IRInstruction> users = new LinkedHashSet<>(getDef().getUsers());
        users.forEach(user -> user.replaceUse(getDef(), value));
    }

    abstract public void forEachNonLabelOperand(Consumer<IROperand> consumer);

    abstract public IRInstruction cloneMySelf(CloneManager m);

    abstract public IRRegister getDef();

    public static void checkRemoved() {
        removed.forEach(inst -> {
            if (inst.getDef() != null) {
                if (!inst.getDef().getUsers().isEmpty()) {
                    inst.getDef().getUsers().forEach(user -> {
                        if (!removed.contains(user)) throw new OptimizeError("removed inst with users :" + inst + " " + inst.getDef().getUsers());
                    });
                }
            }
        });
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

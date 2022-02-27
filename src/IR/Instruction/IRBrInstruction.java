package IR.Instruction;

import IR.IRBasicBlock;
import FrontEnd.IRVisitor;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import MiddleEnd.Utils.CloneManager;
import Utility.error.OptimizeError;

import java.util.function.Consumer;

public class IRBrInstruction extends IRInstruction {
    private IROperand condition;
    private IRBasicBlock thenBlock;
    private IRBasicBlock elseBlock;

    public IRBrInstruction(IRBasicBlock parentBlock, IROperand condition, IRBasicBlock thenBlock, IRBasicBlock elseBlock) {
        super(parentBlock);
        assert parentBlock != null && thenBlock != null && elseBlock != null;
        assert condition.getIRType().isBool();
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        thenBlock.addPredecessor(parentBlock);
        elseBlock.addPredecessor(parentBlock);
        condition.addUser(this);
        thenBlock.getLabel().addUser(this);
        elseBlock.getLabel().addUser(this);
    }

    public IROperand getCondition() {
        return condition;
    }

    public IRBasicBlock getThenBlock() {
        return thenBlock;
    }

    public IRBasicBlock getElseBlock() {
        return elseBlock;
    }

    public void replaceControlFlowTarget(IRBasicBlock oldBlock, IRBasicBlock newBlock) {
        boolean changed = false;
        if (thenBlock == oldBlock) {
            thenBlock = newBlock;
            changed = true;
        }
        if (elseBlock == oldBlock) {
            elseBlock = newBlock;
            changed = true;
        }
        if (!changed) throw new OptimizeError("replaceControlFlowTarget failed at inst: " + this + ", oldBlock: " + oldBlock + ", newBlock: " + newBlock);
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        super.replaceUse(oldOperand, newOperand);
        if (condition == oldOperand) {
            oldOperand.removeUser(this);
            condition = newOperand;
            newOperand.addUser(this);
        }
    }

    @Override
    public void forEachNonLabelOperand(Consumer<IROperand> consumer) {

    }

    @Override
    public IRInstruction cloneMySelf(CloneManager m) {
        return new IRBrInstruction(m.get(getParentBlock()), m.get(condition), m.get(thenBlock), m.get(elseBlock));
    }

    @Override
    public IRRegister getDef() {
        return null;
    }

    @Override
    public boolean noUsersAndSafeToRemove() {
        return false;
    }

    @Override
    public String toString() {
        return "br " + condition.getIRType() + " " + condition + ", label " + thenBlock.getLabel() + ", label " + elseBlock.getLabel();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

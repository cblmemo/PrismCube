package IR.Instruction;

import IR.IRBasicBlock;
import FrontEnd.IRVisitor;
import IR.Operand.IROperand;

public class IRBrInstruction extends IRInstruction {
    private IROperand condition;
    private final IRBasicBlock thenBlock;
    private final IRBasicBlock elseBlock;

    public IRBrInstruction(IRBasicBlock parentBlock, IROperand condition, IRBasicBlock thenBlock, IRBasicBlock elseBlock, IRBasicBlock currentBlock) {
        super(parentBlock);
        assert currentBlock != null && thenBlock != null && elseBlock != null;
        assert condition.getIRType().isBool();
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        thenBlock.addPredecessor(currentBlock);
        elseBlock.addPredecessor(currentBlock);
        condition.addUser(this);
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

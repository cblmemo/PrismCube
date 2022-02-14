package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import Utility.error.OptimizeError;

public class IRJumpInstruction extends IRInstruction {
    private IRBasicBlock targetBlock;

    public IRJumpInstruction(IRBasicBlock parentBlock, IRBasicBlock targetBlock) {
        super(parentBlock);
        this.targetBlock = targetBlock;
        targetBlock.addPredecessor(parentBlock);
    }

    public IRBasicBlock getTargetBlock() {
        return targetBlock;
    }

    public void replaceControlFlowTarget(IRBasicBlock oldBlock, IRBasicBlock newBlock) {
        if (targetBlock == oldBlock) targetBlock = newBlock;
        else throw new OptimizeError("replaceControlFlowTarget failed at inst: " + this + ", targetBlock: " + targetBlock + ", oldBlock: " + oldBlock + ", newBlock: " + newBlock);
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        // do nothing since no use in jump
    }

    @Override
    public boolean noUsersAndSafeToRemove() {
        return false;
    }

    @Override
    public String toString() {
        return "br label " + targetBlock.getLabel();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

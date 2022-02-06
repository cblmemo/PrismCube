package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;

public class IRJumpInstruction extends IRInstruction {
    private final IRBasicBlock targetBlock;

    public IRJumpInstruction(IRBasicBlock parentBlock, IRBasicBlock targetBlock, IRBasicBlock currentBlock) {
        super(parentBlock);
        this.targetBlock = targetBlock;
        targetBlock.addPredecessor(currentBlock);
    }

    public IRBasicBlock getTargetBlock() {
        return targetBlock;
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

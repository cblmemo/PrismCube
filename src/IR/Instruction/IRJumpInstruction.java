package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;

public class IRJumpInstruction extends IRInstruction{
    private final IRBasicBlock targetBlock;

    public IRJumpInstruction(IRBasicBlock targetBlock, IRBasicBlock currentBlock) {
        this.targetBlock = targetBlock;
        targetBlock.addPredecessor(currentBlock);
    }

    public IRBasicBlock getTargetBlock() {
        return targetBlock;
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

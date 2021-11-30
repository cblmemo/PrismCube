package IR.Instruction;

import IR.IRBasicBlock;
import IR.IRVisitor;
import IR.Operand.IROperand;

public class IRBrInstruction extends IRInstruction {
    private final IROperand condition;
    private final IRBasicBlock thenBlock;
    private final IRBasicBlock elseBlock;

    public IRBrInstruction(IROperand condition, IRBasicBlock thenBlock, IRBasicBlock elseBlock, IRBasicBlock currentBlock) {
        assert currentBlock != null;
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        thenBlock.addPredecessor(currentBlock);
        if (elseBlock != null) elseBlock.addPredecessor(currentBlock);
        assert (condition == null) == (elseBlock == null);
        assert condition == null || (condition.getIRType().isBool());
    }

    @Override
    public String toString() {
        if (condition == null) return "br label " + thenBlock.getLabel();
        return "br " + condition.getIRType() + " " + condition + ", label " + thenBlock.getLabel() + ", label " + elseBlock.getLabel();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

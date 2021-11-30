package IR.Instruction;

import IR.IRBasicBlock;
import IR.IRModule;
import IR.IRVisitor;
import IR.Operand.IROperand;

import java.util.Objects;

public class IRBrInstruction extends IRInstruction {
    private final IROperand condition;
    private final IRBasicBlock thenBlock;
    private final IRBasicBlock elseBlock;

    public IRBrInstruction(IROperand condition, IRBasicBlock thenBlock, IRBasicBlock elseBlock, IRModule module) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
        assert (condition == null) == (elseBlock == null);
        assert condition == null || Objects.equals(condition.getIRType(), module.getIRType("bool"));
    }

    @Override
    public String toString() {
        if (condition == null) return "br label " + thenBlock.getLabel();
        return "br i1 " + condition + ", label " + thenBlock.getLabel() + ", label " + elseBlock.getLabel();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

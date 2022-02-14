package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;

public class IRMoveInstruction extends IRInstruction {
    private final IRRegister resultRegister;
    private final IROperand value;

    public IRMoveInstruction(IRBasicBlock parentBlock, IRRegister resultRegister, IROperand value) {
        super(parentBlock);
        this.resultRegister = resultRegister;
        this.value = value;
        value.addUser(this);
        resultRegister.setDef(this);
    }

    public IRRegister getResultRegister() {
        return resultRegister;
    }

    public IROperand getValue() {
        return value;
    }

    @Override
    public boolean noUsersAndSafeToRemove() {
        return resultRegister.getUsers().isEmpty();
    }

    @Override
    public String toString() {
        return resultRegister + " = move " + value;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

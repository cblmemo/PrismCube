package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import MiddleEnd.Utils.CloneManager;

import java.util.function.Consumer;

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
    public void forEachNonLabelOperand(Consumer<IROperand> consumer) {
        consumer.accept(resultRegister);
        consumer.accept(value);
    }

    @Override
    public IRInstruction cloneMySelf(CloneManager m) {
        return new IRMoveInstruction(m.get(getParentBlock()), (IRRegister) m.get(resultRegister), m.get(value));
    }

    @Override
    public IRRegister getDef() {
        return resultRegister;
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

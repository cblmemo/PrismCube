package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IRConstNumber;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import MiddleEnd.Utils.CloneManager;

import java.util.function.Consumer;

public class IRBinaryInstruction extends IRInstruction {
    private final String op;
    private final IRRegister resultRegister;
    private IROperand lhs;
    private IROperand rhs;

    private boolean isLogicBinary = false;

    public IRBinaryInstruction(IRBasicBlock parentBlock, String op, IRRegister resultRegister, IROperand lhs, IROperand rhs) {
        super(parentBlock);
        this.op = op;
        this.resultRegister = resultRegister;
        this.lhs = lhs;
        this.rhs = rhs;
        lhs.addUser(this);
        rhs.addUser(this);
        resultRegister.setDef(this);
    }

    public String getOp() {
        return op;
    }

    public IRRegister getResultRegister() {
        return resultRegister;
    }

    public IROperand getLhs() {
        return lhs;
    }

    public IROperand getRhs() {
        return rhs;
    }

    public IRBinaryInstruction setLogicBinary() {
        isLogicBinary = true;
        return this;
    }

    public boolean isLogicBinary() {
        return isLogicBinary;
    }

    public Integer getConstOtherThan(IROperand operand) {
        if (lhs == operand && rhs != operand)
            return rhs instanceof IRConstNumber ? ((IRConstNumber) rhs).getIntValue() : null;
        if (lhs != operand && rhs == operand)
            return lhs instanceof IRConstNumber ? ((IRConstNumber) lhs).getIntValue() : null;
        return null;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        super.replaceUse(oldOperand, newOperand);
        if (lhs == oldOperand) {
            oldOperand.removeUser(this);
            lhs = newOperand;
            newOperand.addUser(this);
        }
        if (rhs == oldOperand) {
            oldOperand.removeUser(this);
            rhs = newOperand;
            newOperand.addUser(this);
        }
    }

    @Override
    public void forEachNonLabelOperand(Consumer<IROperand> consumer) {
        consumer.accept(resultRegister);
        consumer.accept(lhs);
        consumer.accept(rhs);
    }

    @Override
    public IRInstruction cloneMySelf(CloneManager m) {
        return new IRBinaryInstruction(m.get(getParentBlock()), op, (IRRegister) m.get(resultRegister), m.get(lhs), m.get(rhs));
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
        return resultRegister + " = " + op + " " + lhs.getIRType() + " " + lhs + ", " + rhs;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

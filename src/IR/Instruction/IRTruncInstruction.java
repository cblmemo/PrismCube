package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRTypeSystem;
import Utility.CloneManager;

import java.util.function.Consumer;

public class IRTruncInstruction extends IRInstruction {
    private final IRRegister resultRegister;
    private IROperand truncTarget;
    private final IRTypeSystem originalType;
    private final IRTypeSystem resultType;

    public IRTruncInstruction(IRBasicBlock parentBlock, IRRegister resultRegister, IROperand truncTarget, IRTypeSystem resultType) {
        super(parentBlock);
        assert resultRegister.getIRType().isBool() || resultRegister.getIRType().isChar() || resultRegister.getIRType().isInt();
        assert truncTarget.getIRType().isBool() || truncTarget.getIRType().isChar() || truncTarget.getIRType().isInt();
        assert resultType.isBool() || resultType.isChar() || resultType.isInt();
        this.resultRegister = resultRegister;
        this.truncTarget = truncTarget;
        this.originalType = truncTarget.getIRType();
        this.resultType = resultType;
        truncTarget.addUser(this);
        resultRegister.setDef(this);
    }

    public IRRegister getResultRegister() {
        return resultRegister;
    }

    public IROperand getTruncTarget() {
        return truncTarget;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        super.replaceUse(oldOperand, newOperand);
        if (truncTarget == oldOperand) {
            oldOperand.removeUser(this);
            truncTarget = newOperand;
            newOperand.addUser(this);
        }
    }

    @Override
    public void forEachNonLabelOperand(Consumer<IROperand> consumer) {
        consumer.accept(resultRegister);
        consumer.accept(truncTarget);
    }

    @Override
    public IRInstruction cloneMySelf(CloneManager m) {
        return new IRTruncInstruction(m.get(getParentBlock()), (IRRegister) m.get(resultRegister), m.get(truncTarget), resultType);
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
        return resultRegister + " = trunc " + originalType + " " + truncTarget + " to " + resultType;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRIntType;
import IR.TypeSystem.IRTypeSystem;

public class IRZextInstruction extends IRInstruction {
    private final IRRegister resultRegister;
    private IROperand zextTarget;
    private final IRTypeSystem originalType;
    private final IRTypeSystem resultType;

    public IRZextInstruction(IRBasicBlock parentBlock, IRRegister resultRegister, IROperand zextTarget, IRTypeSystem resultType) {
        super(parentBlock);
        assert resultRegister.getIRType() instanceof IRIntType;
        assert zextTarget.getIRType() instanceof IRIntType;
        assert resultType instanceof IRIntType;
        this.resultRegister = resultRegister;
        this.zextTarget = zextTarget;
        this.originalType = zextTarget.getIRType();
        this.resultType = resultType;
        zextTarget.addUser(this);
        resultRegister.setDef(this);
    }

    public IRRegister getResultRegister() {
        return resultRegister;
    }

    public IROperand getZextTarget() {
        return zextTarget;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        super.replaceUse(oldOperand, newOperand);
        if (zextTarget == oldOperand) {
            oldOperand.removeUser(this);
            zextTarget = newOperand;
            newOperand.addUser(this);
        }
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
        return resultRegister + " = zext " + originalType + " " + zextTarget + " to " + resultType;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

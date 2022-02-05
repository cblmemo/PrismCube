package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRIntType;
import IR.TypeSystem.IRTypeSystem;

public class IRZextInstruction extends IRInstruction {
    private final IRRegister zextResultRegister;
    private IROperand zextTarget;
    private final IRTypeSystem originalType;
    private final IRTypeSystem resultType;

    public IRZextInstruction(IRBasicBlock parentBlock, IRRegister zextResultRegister, IROperand zextTarget, IRTypeSystem resultType) {
        super(parentBlock);
        assert zextResultRegister.getIRType() instanceof IRIntType;
        assert zextTarget.getIRType() instanceof IRIntType;
        assert resultType instanceof IRIntType;
        this.zextResultRegister = zextResultRegister;
        this.zextTarget = zextTarget;
        this.originalType = zextTarget.getIRType();
        this.resultType = resultType;
        zextTarget.addUser(this);
    }

    public IRRegister getZextResultRegister() {
        return zextResultRegister;
    }

    public IROperand getZextTarget() {
        return zextTarget;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        if (zextTarget == oldOperand) {
            oldOperand.removeUser(this);
            zextTarget = newOperand;
            newOperand.addUser(this);
        }
    }

    @Override
    public String toString() {
        return zextResultRegister + " = zext " + originalType + " " + zextTarget + " to " + resultType;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

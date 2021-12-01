package IR.Instruction;

import IR.IRVisitor;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRTypeSystem;

public class IRZextInstruction extends IRInstruction {
    private final IRRegister zextResultRegister;
    private final IROperand zextTarget;
    private final IRTypeSystem originalType;
    private final IRTypeSystem resultType;

    public IRZextInstruction(IRRegister zextResultRegister, IROperand zextTarget, IRTypeSystem resultType) {
        assert zextResultRegister.getIRType().isInt();
        assert zextTarget.getIRType().isInt();
        assert resultType.isInt();
        this.zextResultRegister = zextResultRegister;
        this.zextTarget = zextTarget;
        this.originalType = zextTarget.getIRType();
        this.resultType = resultType;
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

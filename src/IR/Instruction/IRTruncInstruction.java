package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRTypeSystem;

public class IRTruncInstruction extends IRInstruction {
    private final IRRegister truncResultRegister;
    private IROperand truncTarget;
    private final IRTypeSystem originalType;
    private final IRTypeSystem resultType;

    public IRTruncInstruction(IRBasicBlock parentBlock, IRRegister truncResultRegister, IROperand truncTarget, IRTypeSystem resultType) {
        super(parentBlock);
        assert truncResultRegister.getIRType().isBool() || truncResultRegister.getIRType().isChar() || truncResultRegister.getIRType().isInt();
        assert truncTarget.getIRType().isBool() || truncTarget.getIRType().isChar() || truncTarget.getIRType().isInt();
        assert resultType.isBool() || resultType.isChar() || resultType.isInt();
        this.truncResultRegister = truncResultRegister;
        this.truncTarget = truncTarget;
        this.originalType = truncTarget.getIRType();
        this.resultType = resultType;
        truncTarget.addUser(this);
    }

    public IRRegister getTruncResultRegister() {
        return truncResultRegister;
    }

    public IROperand getTruncTarget() {
        return truncTarget;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        if (truncTarget == oldOperand) {
            oldOperand.removeUser(this);
            truncTarget = newOperand;
            newOperand.addUser(this);
        }
    }

    @Override
    public String toString() {
        return truncResultRegister + " = trunc " + originalType + " " + truncTarget + " to " + resultType;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

import java.util.Objects;

public class IRLoadInstruction extends IRInstruction {
    private final IRTypeSystem loadType;
    private final IRRegister loadTarget;
    private final IROperand loadValue;

    public IRLoadInstruction(IRTypeSystem loadType, IRRegister loadTarget, IROperand loadValue) {
        assert loadValue.getIRType() instanceof IRPointerType;
        assert Objects.equals(loadType, ((IRPointerType) loadValue.getIRType()).getBaseType());
        assert Objects.equals(loadType, loadTarget.getIRType());
        this.loadType = loadType;
        this.loadTarget = loadTarget;
        this.loadValue = loadValue;
    }

    public IRRegister getLoadTarget() {
        return loadTarget;
    }

    public IROperand getLoadValue() {
        return loadValue;
    }

    public IRTypeSystem getLoadType() {
        return loadType;
    }

    @Override
    public String toString() {
        return loadTarget + " = load " + loadType + ", " + loadValue.getIRType() + " " + loadValue + (IRInstruction.useAlign() ? ", align " + loadType.sizeof() : "");
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

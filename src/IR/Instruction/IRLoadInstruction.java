package IR.Instruction;

import IR.IRVisitor;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

import java.util.Objects;

public class IRLoadInstruction extends IRInstruction {
    private final IRTypeSystem loadType;
    private final IRRegister loadTarget;
    private final IROperand loadSource;

    public IRLoadInstruction(IRTypeSystem loadType, IRRegister loadTarget, IROperand loadSource) {
        assert loadSource.getIRType() instanceof IRPointerType;
        assert Objects.equals(loadType, ((IRPointerType) loadSource.getIRType()).getBaseType());
        assert Objects.equals(loadType, loadTarget.getIRType());
        this.loadType = loadType;
        this.loadTarget = loadTarget;
        this.loadSource = loadSource;
    }

    @Override
    public String toString() {
        return loadTarget + " = load " + loadType + ", " + loadSource.getIRType() + " " + loadSource + ", align " + loadType.sizeof();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

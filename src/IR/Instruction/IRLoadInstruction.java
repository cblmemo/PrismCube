package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

import java.util.Objects;

public class IRLoadInstruction extends IRInstruction {
    private final IRTypeSystem loadType;
    private final IRRegister loadTarget;
    private IROperand loadAddress;

    public IRLoadInstruction(IRBasicBlock parentBlock, IRTypeSystem loadType, IRRegister loadTarget, IROperand loadAddress) {
        super(parentBlock);
        assert loadAddress.getIRType() instanceof IRPointerType;
        assert Objects.equals(loadType, ((IRPointerType) loadAddress.getIRType()).getBaseType());
        assert Objects.equals(loadType, loadTarget.getIRType());
        this.loadType = loadType;
        this.loadTarget = loadTarget;
        this.loadAddress = loadAddress;
        loadAddress.addUser(this);
    }

    public IRRegister getLoadTarget() {
        return loadTarget;
    }

    public IROperand getLoadAddress() {
        return loadAddress;
    }

    public IRTypeSystem getLoadType() {
        return loadType;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        super.replaceUse(oldOperand, newOperand);
        if (loadAddress == oldOperand) {
            oldOperand.removeUser(this);
            loadAddress = newOperand;
            newOperand.addUser(this);
        }
    }

    @Override
    public String toString() {
        return loadTarget + " = load " + loadType + ", " + loadAddress.getIRType() + " " + loadAddress + (IRInstruction.useAlign() ? ", align " + loadType.sizeof() : "");
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

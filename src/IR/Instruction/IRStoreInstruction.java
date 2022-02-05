package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

import java.util.Objects;

public class IRStoreInstruction extends IRInstruction {
    private final IRTypeSystem storeType;
    private IROperand storeTarget;
    private IROperand storeValue;

    public IRStoreInstruction(IRBasicBlock parentBlock, IRTypeSystem storeType, IROperand storeTarget, IROperand storeValue) {
        super(parentBlock);
        assert storeTarget.getIRType() instanceof IRPointerType;
        assert Objects.equals(storeType, ((IRPointerType) storeTarget.getIRType()).getBaseType());
        assert Objects.equals(storeType, storeValue.getIRType());
        this.storeType = storeType;
        this.storeTarget = storeTarget;
        this.storeValue = storeValue;
        storeTarget.addUser(this);
        storeValue.addUser(this);
    }

    public IROperand getStoreTarget() {
        return storeTarget;
    }

    public IROperand getStoreValue() {
        return storeValue;
    }

    public IRTypeSystem getStoreType() {
        return storeType;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        if (storeTarget == oldOperand) {
            oldOperand.removeUser(this);
            storeTarget = newOperand;
            newOperand.addUser(this);
        }
        if (storeValue == oldOperand) {
            oldOperand.removeUser(this);
            storeValue = newOperand;
            newOperand.addUser(this);
        }
    }

    @Override
    public String toString() {
        return "store " + storeType + " " + storeValue + ", " + storeType + "* " + storeTarget + (IRInstruction.useAlign() ? (", align " + storeType.sizeof()) : "");
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

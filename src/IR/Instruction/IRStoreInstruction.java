package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

import java.util.Objects;

public class IRStoreInstruction extends IRInstruction {
    private final IRTypeSystem storeType;
    private IROperand storeAddress;
    private IROperand storeValue;

    public IRStoreInstruction(IRBasicBlock parentBlock, IRTypeSystem storeType, IROperand storeAddress, IROperand storeValue) {
        super(parentBlock);
        assert storeAddress.getIRType() instanceof IRPointerType;
        assert Objects.equals(storeType, ((IRPointerType) storeAddress.getIRType()).getBaseType());
        assert Objects.equals(storeType, storeValue.getIRType());
        this.storeType = storeType;
        this.storeAddress = storeAddress;
        this.storeValue = storeValue;
        storeAddress.addUser(this);
        storeValue.addUser(this);
    }

    public IROperand getStoreAddress() {
        return storeAddress;
    }

    public IROperand getStoreValue() {
        return storeValue;
    }

    public IRTypeSystem getStoreType() {
        return storeType;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        super.replaceUse(oldOperand, newOperand);
        if (storeAddress == oldOperand) {
            oldOperand.removeUser(this);
            storeAddress = newOperand;
            newOperand.addUser(this);
        }
        if (storeValue == oldOperand) {
            oldOperand.removeUser(this);
            storeValue = newOperand;
            newOperand.addUser(this);
        }
    }

    @Override
    public IRRegister getDef() {
        return null;
    }

    @Override
    public boolean noUsersAndSafeToRemove() {
        return false;
    }

    @Override
    public String toString() {
        return "store " + storeType + " " + storeValue + ", " + storeType + "* " + storeAddress + (IRInstruction.useAlign() ? (", align " + storeType.sizeof()) : "");
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

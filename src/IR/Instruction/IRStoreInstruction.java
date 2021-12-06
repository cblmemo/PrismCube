package IR.Instruction;

import IR.IRVisitor;
import IR.Operand.IROperand;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

import java.util.Objects;

public class IRStoreInstruction extends IRInstruction {
    private final IRTypeSystem storeType;
    private final IROperand storeTarget;
    private final IROperand storeValue;

    public IRStoreInstruction(IRTypeSystem storeType, IROperand storeTarget, IROperand storeValue) {
        assert storeTarget.getIRType() instanceof IRPointerType;
        assert Objects.equals(storeType, ((IRPointerType) storeTarget.getIRType()).getBaseType());
        assert Objects.equals(storeType, storeValue.getIRType());
        this.storeType = storeType;
        this.storeTarget = storeTarget;
        this.storeValue = storeValue;
    }

    @Override
    public String toString() {
        return "store " + storeType + " " + storeValue + ", " + storeType + "* " + storeTarget.toString() + ", align " + storeType.sizeof();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

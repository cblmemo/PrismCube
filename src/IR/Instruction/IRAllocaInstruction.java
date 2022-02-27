package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;
import MiddleEnd.Utils.CloneManager;

import java.util.Objects;
import java.util.function.Consumer;

public class IRAllocaInstruction extends IRInstruction {
    private final IRTypeSystem allocaType;
    private final IRRegister allocaTarget;

    public IRAllocaInstruction(IRBasicBlock parentBlock, IRTypeSystem allocaType, IRRegister allocaTarget) {
        super(parentBlock);
        assert allocaTarget.getIRType() instanceof IRPointerType;
        assert Objects.equals(allocaType, ((IRPointerType) allocaTarget.getIRType()).getBaseType());
        this.allocaType = allocaType;
        this.allocaTarget = allocaTarget;
        allocaTarget.setDef(this);
    }

    public IRTypeSystem getAllocaType() {
        return allocaType;
    }

    public IRRegister getAllocaTarget() {
        return allocaTarget;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        // do nothing since no use in alloca
    }

    @Override
    public void forEachNonLabelOperand(Consumer<IROperand> consumer) {
        consumer.accept(allocaTarget);
    }

    @Override
    public IRInstruction cloneMySelf(CloneManager m) {
        return new IRAllocaInstruction(m.get(getParentBlock()), allocaType, (IRRegister) m.get(allocaTarget));
    }

    @Override
    public IRRegister getDef() {
        return allocaTarget;
    }

    @Override
    public boolean noUsersAndSafeToRemove() {
        return false;
    }

    @Override
    public String toString() {
        return allocaTarget + " = alloca " + allocaType + (IRInstruction.useAlign() ? (", align " + allocaType.sizeof()) : "");
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

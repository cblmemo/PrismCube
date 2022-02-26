package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;
import Utility.CloneManager;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Consumer;

public class IRBitcastInstruction extends IRInstruction {
    private final IRRegister resultRegister;
    private IROperand ptrValue;
    private final IRTypeSystem targetType;

    public IRBitcastInstruction(IRBasicBlock parentBlock, IRRegister resultRegister, IROperand ptrValue, IRTypeSystem targetType) {
        super(parentBlock);
        assert Objects.equals(resultRegister.getIRType(), targetType);
        assert targetType instanceof IRPointerType;
        assert ptrValue.getIRType() instanceof IRPointerType;
        this.resultRegister = resultRegister;
        this.ptrValue = ptrValue;
        this.targetType = targetType;
        ptrValue.addUser(this);
        resultRegister.setDef(this);
    }

    public IRRegister getResultRegister() {
        return resultRegister;
    }

    public IROperand getPtrValue() {
        return ptrValue;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        super.replaceUse(oldOperand, newOperand);
        if (ptrValue == oldOperand) {
            oldOperand.removeUser(this);
            ptrValue = newOperand;
            newOperand.addUser(this);
        }
    }

    @Override
    public void forEachNonLabelOperand(Consumer<IROperand> consumer) {
        consumer.accept(resultRegister);
        consumer.accept(ptrValue);
    }

    @Override
    public IRInstruction cloneMySelf(CloneManager m) {
        return new IRBitcastInstruction(m.get(getParentBlock()), (IRRegister) m.get(resultRegister), m.get(ptrValue), targetType);
    }

    @Override
    public IRRegister getDef() {
        return resultRegister;
    }

    @Override
    public boolean noUsersAndSafeToRemove() {
        return resultRegister.getUsers().isEmpty();
    }

    @Override
    public String toString() {
        return resultRegister + " = bitcast " + ptrValue.getIRType() + " " + ptrValue + " to " + targetType;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

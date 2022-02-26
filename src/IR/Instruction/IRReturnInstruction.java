package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRTypeSystem;
import Utility.CloneManager;
import Utility.error.OptimizeError;

import java.util.Objects;
import java.util.function.Consumer;

public class IRReturnInstruction extends IRInstruction {
    private final IRTypeSystem returnType;
    private IROperand returnValue;

    public IRReturnInstruction(IRBasicBlock parentBlock, IRTypeSystem returnType, IROperand returnValue) {
        super(parentBlock);
        if (returnType.isVoid()) assert returnValue == null;
        else assert Objects.equals(returnType, returnValue.getIRType());
        this.returnType = returnType;
        this.returnValue = returnValue;
        if (returnValue != null) returnValue.addUser(this);
    }

    public boolean hasReturnValue() {
        return !returnType.isVoid();
    }

    public IROperand getReturnValue() {
        return returnValue;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        super.replaceUse(oldOperand, newOperand);
        if (returnValue == oldOperand) {
            oldOperand.removeUser(this);
            returnValue = newOperand;
            newOperand.addUser(this);
        }
    }

    @Override
    public void forEachNonLabelOperand(Consumer<IROperand> consumer) {
        consumer.accept(returnValue);
    }

    @Override
    public IRInstruction cloneMySelf(CloneManager m) {
        throw new OptimizeError("unexpected clone of return " + this);
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
        if (returnType.isVoid()) return "ret void";
        return "ret " + returnType + " " + returnValue;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

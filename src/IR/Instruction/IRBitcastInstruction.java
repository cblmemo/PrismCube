package IR.Instruction;

import IR.IRVisitor;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

import java.util.Objects;

public class IRBitcastInstruction extends IRInstruction {
    private final IRRegister resultRegister;
    private final IROperand ptrValue;
    private final IRTypeSystem targetType;

    public IRBitcastInstruction(IRRegister resultRegister, IROperand ptrValue, IRTypeSystem targetType) {
        assert Objects.equals(resultRegister.getIRType(), targetType);
        assert targetType instanceof IRPointerType;
        assert ptrValue.getIRType() instanceof IRPointerType;
        this.resultRegister = resultRegister;
        this.ptrValue = ptrValue;
        this.targetType = targetType;
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

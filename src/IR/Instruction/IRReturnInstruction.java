package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.Operand.IROperand;
import IR.TypeSystem.IRTypeSystem;

import java.util.Objects;

public class IRReturnInstruction extends IRInstruction {
    private final IRTypeSystem returnType;
    private final IROperand returnValue;

    public IRReturnInstruction(IRTypeSystem returnType, IROperand returnValue) {
        if (returnType.isVoid()) assert returnValue == null;
        else assert Objects.equals(returnType, returnValue.getIRType());
        this.returnType = returnType;
        this.returnValue = returnValue;
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

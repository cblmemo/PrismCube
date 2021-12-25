package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

import java.util.Objects;

public class IRAllocaInstruction extends IRInstruction {
    private final IRTypeSystem allocaType;
    private final IRRegister allocaTarget;

    public IRAllocaInstruction(IRTypeSystem allocaType, IRRegister allocaTarget) {
        assert allocaTarget.getIRType() instanceof IRPointerType;
        assert Objects.equals(allocaType, ((IRPointerType) allocaTarget.getIRType()).getBaseType());
        this.allocaType = allocaType;
        this.allocaTarget = allocaTarget;
    }

    @Override
    public String toString() {
        return allocaTarget + " = alloca " + allocaType + ", align " + allocaType.sizeof();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

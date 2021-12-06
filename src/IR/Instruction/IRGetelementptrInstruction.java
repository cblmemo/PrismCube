package IR.Instruction;

import IR.IRVisitor;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

import java.util.ArrayList;
import java.util.Objects;

public class IRGetelementptrInstruction extends IRInstruction {
    private final IRRegister resultRegister;
    private final IRTypeSystem elementType;
    private final IROperand ptrValue;
    private final ArrayList<IROperand> indices = new ArrayList<>();
    boolean inbounds;

    public IRGetelementptrInstruction(IRRegister resultRegister, IRTypeSystem elementType, IROperand ptrValue) {
        assert ptrValue.getIRType() instanceof IRPointerType;
        assert Objects.equals(elementType, ((IRPointerType) ptrValue.getIRType()).getBaseType());
        assert Objects.equals(resultRegister.getIRType(), ptrValue.getIRType());
        this.resultRegister = resultRegister;
        this.elementType = elementType;
        this.ptrValue = ptrValue;
        this.inbounds = true;
    }

    public void addIndex(IROperand index) {
        this.indices.add(index);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(resultRegister.toString()).append(" = getelementptr ");
        if (inbounds) builder.append("inbounds ");
        builder.append(elementType.toString()).append(", ").append(ptrValue.getIRType()).append(" ").append(ptrValue);
        indices.forEach(index -> builder.append(", ").append(index.getIRType().toString()).append(" ").append(index));
        return builder.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

import java.util.ArrayList;
import java.util.Objects;

public class IRGetelementptrInstruction extends IRInstruction {
    private final IRRegister resultRegister;
    private final IRTypeSystem elementType;
    private IROperand ptrValue;
    private final ArrayList<IROperand> indices = new ArrayList<>();

    public IRGetelementptrInstruction(IRBasicBlock parentBlock, IRRegister resultRegister, IRTypeSystem elementType, IROperand ptrValue) {
        super(parentBlock);
        assert ptrValue.getIRType() instanceof IRPointerType;
        assert Objects.equals(elementType, ((IRPointerType) ptrValue.getIRType()).getBaseType());
        this.resultRegister = resultRegister;
        this.elementType = elementType;
        this.ptrValue = ptrValue;
        ptrValue.addUser(this);
    }

    public void addIndex(IROperand index) {
        this.indices.add(index);
        index.addUser(this);
    }

    public ArrayList<IROperand> getIndices() {
        return indices;
    }

    public IRRegister getResultRegister() {
        return resultRegister;
    }

    public IRTypeSystem getElementType() {
        return elementType;
    }

    public IROperand getPtrValue() {
        return ptrValue;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        if (ptrValue == oldOperand) {
            oldOperand.removeUser(this);
            ptrValue = newOperand;
            newOperand.addUser(this);
        }
        for (int i = 0; i < indices.size(); i++) {
            if (indices.get(i) == oldOperand) {
                oldOperand.removeUser(this);
                indices.set(i, newOperand);
                newOperand.addUser(this);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(resultRegister).append(" = getelementptr inbounds ");
        builder.append(elementType).append(", ").append(ptrValue.getIRType()).append(" ").append(ptrValue);
        indices.forEach(index -> builder.append(", ").append(index.getIRType()).append(" ").append(index));
        return builder.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

package ASM.Instruction;

import ASM.ASMVisitor;
import ASM.Operand.ASMOperand;

import java.util.ArrayList;

public class ASMPseudoInstruction extends ASMInstruction {
    public enum InstType {
        li
    }

    private final InstType type;
    private final ArrayList<ASMOperand> operands = new ArrayList<>();

    public ASMPseudoInstruction(InstType type) {
        this.type = type;
    }

    public ASMPseudoInstruction addOperand(ASMOperand operand) {
        operands.add(operand);
        return this;
    }

    private String getInstStr() {
        return ASMInstruction.align(type.toString());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getInstStr());
        for (int i = 0; i < operands.size(); i++) {
            if (i != 0) builder.append(", ");
            builder.append(operands.get(i).toString());
        }
        return builder.toString();
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}

package ASM.Instruction;

import ASM.Operand.ASMOperand;
import BackEnd.ASMPrinter;

import java.util.ArrayList;
import java.util.Objects;

abstract public class ASMInstruction {
    private static String align(String inst) {
        return inst + " ".repeat(ASMPrinter.getAlignLength() - inst.length());
    }

    private final ArrayList<ASMOperand> operands = new ArrayList<>();
    private final String instStr;

    public ASMInstruction(String instStr) {
        this.instStr = instStr;
    }

    public ASMInstruction addOperand(ASMOperand operand) {
        operands.add(operand);
        return this;
    }

    public ArrayList<ASMOperand> getOperands() {
        return operands;
    }

    public void setOperand(int index, ASMOperand operand) {
        operands.set(index, operand);
    }

    public boolean isStoreInstruction() {
        return Objects.equals(instStr, "sb") || Objects.equals(instStr, "sw");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(align(instStr));
        for (int i = 0; i < operands.size(); i++) {
            if (i != 0) builder.append(", ");
            builder.append(operands.get(i).toString());
        }
        return builder.toString();
    }
}

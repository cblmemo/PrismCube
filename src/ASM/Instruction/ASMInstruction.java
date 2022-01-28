package ASM.Instruction;

import ASM.Operand.ASMAddress;
import ASM.Operand.ASMOperand;
import ASM.Operand.ASMPhysicalRegister;
import ASM.Operand.ASMRegister;
import BackEnd.ASMEmitter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;

abstract public class ASMInstruction {
    private static String align(String inst) {
        return inst + " ".repeat(ASMEmitter.getAlignLength() - inst.length());
    }

    private final String instStr;
    private final ArrayList<ASMOperand> operands = new ArrayList<>();
    private final ArrayList<ASMRegister> defs = new ArrayList<>();
    private final ArrayList<ASMRegister> uses = new ArrayList<>();

    public ASMInstruction(String instStr) {
        this.instStr = instStr;
        if (Objects.equals(instStr, "call")) ASMPhysicalRegister.getCallerSaveRegisters().forEach(this::addDef);
    }

    protected void addDef(ASMRegister reg) {
        if (reg.countAsDefUse()) defs.add(reg);
    }

    protected void addUse(ASMRegister reg) {
        if (reg.countAsDefUse()) uses.add(reg);
    }

    public ArrayList<ASMRegister> getDefs() {
        return defs;
    }

    public ArrayList<ASMRegister> getUses() {
        return uses;
    }

    public LinkedHashSet<ASMRegister> getDefsAndUses() {
        LinkedHashSet<ASMRegister> ret = new LinkedHashSet<>();
        ret.addAll(defs);
        ret.addAll(uses);
        return ret;
    }

    public ASMInstruction addOperand(ASMOperand operand) {
        operands.add(operand);
        if (operand instanceof ASMRegister || operand instanceof ASMAddress) {
            ASMRegister reg;
            if (operand instanceof ASMRegister) reg = (ASMRegister) operand;
            else reg = ((ASMAddress) operand).getRegister();
            switch (instStr) {
                case "lb", "lw", "li", "mv", "la", "seqz", "snez", "lui", "auipc", "sub", "add", "sll", "slt", "sltu", "xor", "srl", "sra", "or", "and", "addi", "slli", "slti", "sltiu", "xori", "srli", "srai", "ori", "andi", "mul", "div", "rem" -> {
                    if (operands.size() == 1) addDef(reg);
                    else addUse(reg);
                }
                case "sb", "sw", "beqz" -> addUse(reg);
            }
        }
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

    public boolean isBranchInstruction() {
        return Objects.equals(instStr, "beqz");
    }

    public void replaceRegistersWithColor(LinkedHashMap<ASMRegister, ASMPhysicalRegister> color) {
        for (int i = 0; i < operands.size(); i++)
            if (operands.get(i) instanceof ASMRegister) operands.set(i, color.get((ASMRegister) operands.get(i)));
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

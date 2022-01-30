package ASM.Instruction;

import ASM.Operand.*;
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

    protected void removeDef(ASMRegister reg) {
        defs.remove(reg);
    }

    protected void removeUse(ASMRegister reg) {
        uses.remove(reg);
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
                case "sb", "sw", "beqz" -> addUse(reg);
                default -> {
                    if (operands.size() == 1) addDef(reg);
                    else addUse(reg);
                }
            }
        }
        return this;
    }

    public ArrayList<ASMOperand> getOperands() {
        return operands;
    }

    public void replaceRegistersWithColor(LinkedHashMap<ASMRegister, ASMPhysicalRegister> color) {
        for (ASMOperand operand : operands) {
            if (operand instanceof ASMVirtualRegister) replaceRegister((ASMVirtualRegister) operand, color.get((ASMRegister) operand));
            else if (operand instanceof ASMAddress && ((ASMAddress) operand).getRegister() instanceof ASMVirtualRegister)
                replaceRegister((ASMVirtualRegister) ((ASMAddress) operand).getRegister(), color.get(((ASMAddress) operand).getRegister()));
        }
    }

    private void setOperand(int index, ASMOperand operand) {
        operands.set(index, operand);
    }

    private boolean isStoreInstruction() {
        return Objects.equals(instStr, "sb") || Objects.equals(instStr, "sw");
    }

    private boolean isBranchInstruction() {
        return Objects.equals(instStr, "beqz");
    }

    private void replaceDef(int index, ASMVirtualRegister oldReg, ASMRegister newReg) {
        if (getOperands().get(index) == oldReg) {
            setOperand(index, newReg);
            removeDef(oldReg);
            addDef(newReg);
        }
    }

    private void replaceUse(int index, ASMVirtualRegister oldReg, ASMRegister newReg) {
        if (getOperands().get(index) == oldReg) {
            setOperand(index, newReg);
            removeUse(oldReg);
            addUse(newReg);
        }
    }

    public void replaceRegister(ASMVirtualRegister oldReg, ASMRegister newReg) {
        if (this instanceof ASMArithmeticInstruction || this instanceof ASMPseudoInstruction) {
            if (isBranchInstruction()) replaceUse(0, oldReg, newReg);
            else {
                replaceDef(0, oldReg, newReg);
                for (int i = 1; i < getOperands().size(); i++) replaceUse(i, oldReg, newReg);
            }
        } else { // this instanceof ASMMemoryInstruction
            if (isStoreInstruction()) replaceUse(0, oldReg, newReg);
            else replaceDef(0, oldReg, newReg);
            if (((ASMAddress) getOperands().get(1)).getRegister() == oldReg) {
                ((ASMAddress) getOperands().get(1)).replaceRegister(newReg);
                removeUse(oldReg);
                addUse(newReg);
            }
        }
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

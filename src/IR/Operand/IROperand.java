package IR.Operand;

import IR.Instruction.IRAllocaInstruction;
import IR.Instruction.IRBitcastInstruction;
import IR.Instruction.IRGetelementptrInstruction;
import IR.Instruction.IRInstruction;
import IR.TypeSystem.IRTypeSystem;

import java.util.LinkedHashSet;

import static Debug.MemoLog.log;

abstract public class IROperand {
    private final IRTypeSystem irType;
    private final LinkedHashSet<IRInstruction> users = new LinkedHashSet<>();
    private IRInstruction def = null;

    public IROperand(IRTypeSystem irType) {
        this.irType = irType;
    }

    public IRTypeSystem getIRType() {
        return irType;
    }

    public void addUser(IRInstruction instruction) {
        users.add(instruction);
        instruction.addUse(this);
    }

    public void removeUser(IRInstruction instruction) {
        users.remove(instruction);
    }

    public LinkedHashSet<IRInstruction> getUsers() {
        return users;
    }

    public void setDef(IRInstruction def) {
        this.def = def;
    }

    public IRInstruction getDef() {
        return def;
    }

    public IRAllocaInstruction getAllocaDef() {
        if (def instanceof IRAllocaInstruction) return (IRAllocaInstruction) def;
        if (def instanceof IRBitcastInstruction) return ((IRBitcastInstruction) def).getPtrValue().getAllocaDef();
        if (def instanceof IRGetelementptrInstruction) return ((IRGetelementptrInstruction) def).getPtrValue().getAllocaDef();
        log.Tracef("failed when getAllocaDef, operand: %s\n", this);
        return null;
    }

    @Override
    abstract public String toString();
}

package IR.Operand;

import IR.Instruction.IRInstruction;
import IR.TypeSystem.IRTypeSystem;

import java.util.LinkedHashSet;

abstract public class IROperand {
    private final IRTypeSystem irType;
    private final LinkedHashSet<IRInstruction> users = new LinkedHashSet<>();

    public IROperand(IRTypeSystem irType) {
        this.irType = irType;
    }

    public IRTypeSystem getIRType() {
        return irType;
    }

    public void addUser(IRInstruction instruction) {
        users.add(instruction);
        instruction.addUser(this);
    }

    public void removeUser(IRInstruction instruction) {
        users.remove(instruction);
    }

    public LinkedHashSet<IRInstruction> getUsers() {
        return users;
    }

    @Override
    abstract public String toString();
}

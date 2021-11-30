package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

public class IRGlobalVariableRegister extends IRRegister {
    private final String globalVariableName;

    public IRGlobalVariableRegister(IRTypeSystem irType, String globalVariableName) {
        super(irType);
        this.globalVariableName = globalVariableName;
    }

    @Override
    public String toString() {
        return "@" + globalVariableName;
    }
}

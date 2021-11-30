package IR;

import IR.Operand.IROperand;
import IR.TypeSystem.IRIntType;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;

public class IRGlobalDefine {
    private final String variableName;
    private final IRTypeSystem variableType;
    private IROperand initValue;

    public IRGlobalDefine(String variableName, IRTypeSystem variableType) {
        this.variableName = variableName;
        this.variableType = variableType;
        initValue = variableType.getDefaultValue();
    }

    @Override
    public String toString() {
        if (variableType instanceof IRIntType || variableType instanceof IRPointerType || variableType.isString())
            return "@" + variableName + " = global " + variableType + " " + initValue;
        // todo support struct
        return "";
    }

    public String getVariableName() {
        return variableName;
    }

    public void setInitValue(IROperand initValue) {
        this.initValue = initValue;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

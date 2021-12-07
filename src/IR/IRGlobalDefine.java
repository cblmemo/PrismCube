package IR;

import IR.Operand.IROperand;
import IR.TypeSystem.IRIntType;
import IR.TypeSystem.IRTypeSystem;

public class IRGlobalDefine {
    private final String variableName;
    private final IRTypeSystem variableType;
    private final IROperand initValue;

    public IRGlobalDefine(String variableName, IRTypeSystem variableType) {
        this.variableName = variableName;
        this.variableType = variableType;
        initValue = variableType.getDefaultValue();
    }

    @Override
    public String toString() {
        if (variableType instanceof IRIntType || variableType.isArray() || variableType.isString())
            return "@" + variableName + " = global " + variableType + " " + initValue + ", align " + variableType.sizeof();
        return "@" + variableName + " = common global " + variableType + " " + initValue + ", align " + variableType.sizeof();
    }

    public String getVariableName() {
        return variableName;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

package IR.Instruction;

import IR.IRFunction;
import IR.IRVisitor;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRTypeSystem;

import java.util.ArrayList;
import java.util.Objects;

public class IRCallInstruction extends IRInstruction {
    private final IRTypeSystem returnType;
    private final IRFunction callFunction;
    private final ArrayList<IROperand> argumentValues = new ArrayList<>();
    private final ArrayList<IRTypeSystem> argumentTypes = new ArrayList<>();
    private IRRegister resultRegister = null;

    public IRCallInstruction(IRTypeSystem returnType, IRFunction callFunction) {
        this.returnType = returnType;
        this.callFunction = callFunction;
    }

    public IRCallInstruction addArgument(IROperand argumentValue, IRTypeSystem argumentType) {
        assert Objects.equals(argumentType, argumentValue.getIRType());
        argumentValues.add(argumentValue);
        argumentTypes.add(argumentType);
        return this;
    }

    public void setResultRegister(IRRegister resultRegister) {
        assert Objects.equals(resultRegister.getIRType(), returnType);
        this.resultRegister = resultRegister;
    }

    private String getParameterListStr() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (int i = 0; i < argumentValues.size(); i++) {
            if (i != 0) builder.append(", ");
            builder.append(argumentTypes.get(i).toString()).append(" ").append(argumentValues.get(i).toString());
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String toString() {
        String callBody = "call " + returnType.toString() + " @" + callFunction.getFunctionName() + getParameterListStr();
        if (resultRegister == null) return callBody;
        return resultRegister + " = " + callBody;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

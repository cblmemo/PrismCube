package IR.Instruction;

import IR.IRFunction;
import FrontEnd.IRVisitor;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRTypeSystem;

import java.util.ArrayList;
import java.util.Objects;

public class IRCallInstruction extends IRInstruction {
    private final IRTypeSystem returnType;
    private final IRFunction callFunction;
    private final ArrayList<IROperand> argumentValues = new ArrayList<>();
    private int currentArgumentNumber = 0;
    private IRRegister resultRegister = null;

    public IRCallInstruction(IRTypeSystem returnType, IRFunction callFunction) {
        // avoid to print redundant function declare
        callFunction.markAsCalled();
        this.returnType = returnType;
        this.callFunction = callFunction;
    }

    public IRCallInstruction addArgument(IROperand argumentValue, IRTypeSystem argumentType) {
        assert Objects.equals(argumentType, argumentValue.getIRType());
        assert Objects.equals(callFunction.getParameterType().get(currentArgumentNumber), argumentType);
        argumentValues.add(argumentValue);
        currentArgumentNumber++;
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
            builder.append(callFunction.getParameterType().get(i)).append(" ").append(argumentValues.get(i));
        }
        builder.append(")");
        return builder.toString();
    }

    public ArrayList<IROperand> getArgumentValues() {
        return argumentValues;
    }

    public IRFunction getCallFunction() {
        return callFunction;
    }

    public boolean haveReturnValue() {
        return resultRegister != null;
    }

    public IRRegister getResultRegister() {
        return resultRegister;
    }

    public int getArgumentNumber() {
        return argumentValues.size();
    }

    @Override
    public String toString() {
        assert currentArgumentNumber == callFunction.getParameterType().size();
        String callBody = "call " + returnType.toString() + " @" + callFunction.getFunctionName() + getParameterListStr();
        if (resultRegister == null) return callBody;
        return resultRegister + " = " + callBody;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

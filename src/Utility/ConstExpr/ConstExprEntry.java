package Utility.ConstExpr;

import IR.IRModule;
import IR.Operand.*;
import Utility.error.IRError;

public class ConstExprEntry {
    public enum ConstExprValueType {
        intType, boolType, stringType, nullType
    }

    private final boolean isConstexpr;
    private ConstExprValueType constexprValueType;
    private Object constexprValue;

    public static final ConstExprEntry nonConstExprEntry = new ConstExprEntry(false);
    public static final ConstExprEntry nullConstExprEntry = new ConstExprEntry(true, ConstExprValueType.nullType, new NullConstExpr());

    public ConstExprEntry(boolean isConstexpr) {
        this.isConstexpr = isConstexpr;
    }

    public ConstExprEntry(boolean isConstexpr, ConstExprValueType constexprValueType, Object constexprValue) {
        this.isConstexpr = isConstexpr;
        this.constexprValueType = constexprValueType;
        this.constexprValue = constexprValue;
        assert isConstexpr && ((constexprValueType == ConstExprValueType.intType) && (constexprValue instanceof Integer)
                || (constexprValueType == ConstExprValueType.boolType) && (constexprValue instanceof Boolean)
                || (constexprValueType == ConstExprValueType.stringType) && (constexprValue instanceof String)
                || (constexprValueType == ConstExprValueType.nullType) && (constexprValue instanceof NullConstExpr));
    }

    public boolean isConstexpr() {
        return isConstexpr;
    }

    public boolean isNull() {
        return isConstexpr() && constexprValueType == ConstExprValueType.nullType;
    }

    public ConstExprValueType getConstexprValueType() {
        return constexprValueType;
    }

    public Object getConstexprValue() {
        return constexprValue;
    }

    public IROperand toIROperand(IRModule module) {
        switch (constexprValueType) {
            case intType -> {
                return new IRConstInt(module.getIRType("int"), (Integer) constexprValue);
            }
            case boolType -> {
                return new IRConstBool(module.getIRType("bool"), (Boolean) constexprValue);
            }
            case stringType -> {
                return new IRConstString(module.getIRType("string"), (String) constexprValue, module.getConstStringId((String) constexprValue));
            }
            case nullType -> {
                // null only initialize pointer type variable
                // (struct and array use zeroinitializer)
                return new IRNull(module.getIRType("void *"));
            }
        }
        throw new IRError("constexpr entry to ir type failed");
    }
}

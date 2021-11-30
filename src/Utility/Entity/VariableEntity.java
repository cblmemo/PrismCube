package Utility.Entity;

import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import Utility.ConstExpr.ConstExprEntry;
import Utility.Cursor;
import Utility.Type.Type;

public class VariableEntity extends Entity {
    private final Type variableType;

    // for ir
    private ConstExprEntry constexprEntry;
    private boolean notPrint = true;
    private IRRegister currentRegister;

    public VariableEntity(Type variableType, String entityName, Cursor cursor) {
        super(entityName, cursor);
        this.variableType = variableType;
    }

    public Type getVariableType() {
        return variableType;
    }

    public void setConstexprEntry(ConstExprEntry constexprEntry) {
        this.constexprEntry = constexprEntry;
    }

    public boolean notPrint() {
        return notPrint;
    }

    public void markAsPrinted() {
        this.notPrint = false;
    }

    public ConstExprEntry getConstexprEntry() {
        return constexprEntry;
    }

    public void setCurrentRegister(IRRegister currentRegister) {
        this.currentRegister = currentRegister;
    }

    public IRRegister getCurrentRegister() {
        return currentRegister;
    }
}

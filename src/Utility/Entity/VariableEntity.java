package Utility.Entity;

import IR.Operand.IRRegister;
import Utility.Cursor;
import Utility.Type.Type;

public class VariableEntity extends Entity {
    private final Type variableType;

    // for ir
    private boolean notPrint = true;
    private IRRegister currentRegister;

    public VariableEntity(Type variableType, String entityName, Cursor cursor) {
        super(entityName, cursor);
        this.variableType = variableType;
    }

    public Type getVariableType() {
        return variableType;
    }

    public boolean notPrint() {
        return notPrint;
    }

    public void markAsPrinted() {
        this.notPrint = false;
    }

    public void setCurrentRegister(IRRegister currentRegister) {
        this.currentRegister = currentRegister;
    }

    public IRRegister getCurrentRegister() {
        return currentRegister;
    }
}

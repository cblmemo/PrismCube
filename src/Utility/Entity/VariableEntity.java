package Utility.Entity;

import IR.Operand.IRRegister;
import IR.TypeSystem.IRStructureType;
import Utility.Cursor;
import Utility.Type.Type;

public class VariableEntity extends Entity {
    private final Type variableType;

    // for ir
    private IRRegister currentRegister = null;
    private boolean classMember = false;
    private boolean visitedInIR = false;
    private IRStructureType classIRType;
    private int index;

    public VariableEntity(Type variableType, String entityName, Cursor cursor) {
        super(entityName, cursor);
        this.variableType = variableType;
    }

    public void setAsClassMember() {
        classMember = true;
    }

    public boolean isClassMember() {
        return classMember;
    }

    public void addClassMemberInfo(IRStructureType classIRType, int index) {
        this.classIRType = classIRType;
        this.index = index;
    }

    public void setAsVisitedInIR() {
        visitedInIR = true;
    }

    public boolean visitedInIR() {
        return visitedInIR;
    }

    public int getIndex() {
        return index;
    }

    public IRStructureType getClassIRType() {
        return classIRType;
    }

    public Type getVariableType() {
        return variableType;
    }

    public void setCurrentRegister(IRRegister currentRegister) {
        this.currentRegister = currentRegister;
    }

    public IRRegister getCurrentRegister() {
        return currentRegister;
    }
}

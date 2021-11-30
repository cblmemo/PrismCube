package Utility.Entity;

import IR.IRFunction;
import Utility.Cursor;
import Utility.Scope.FunctionScope;
import Utility.Type.Type;


public class FunctionEntity extends MethodEntity {
    private final FunctionScope functionScope;

    // for ir
    private IRFunction irFunction;

    public FunctionEntity(FunctionScope functionScope, String entityName, Cursor cursor) {
        super(entityName, cursor);
        this.functionScope = functionScope;
    }

    public FunctionEntity addParameter(VariableEntity entity) {
        functionScope.addParameter(entity);
        return this;
    }

    public VariableEntity getParameter(int index) {
        return functionScope.getParameter(index);
    }

    public void setReturnType(Type returnType) {
        functionScope.setReturnType(returnType);
    }

    public Type getReturnType() {
        return functionScope.getReturnType();
    }

    public FunctionScope getFunctionScope() {
        return functionScope;
    }

    // for ir

    public void setIRFunction(IRFunction irFunction) {
        this.irFunction = irFunction;
    }

    public IRFunction getIRFunction() {
        return irFunction;
    }
}

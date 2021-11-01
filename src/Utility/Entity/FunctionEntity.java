package Utility.Entity;

import Utility.Cursor;
import Utility.Scope.FunctionScope;
import Utility.Type.Type;


public class FunctionEntity extends MethodEntity {
    private FunctionScope functionScope;

    public FunctionEntity(FunctionScope functionScope, String entityName, Cursor cursor) {
        super(entityName, cursor);
        this.functionScope = functionScope;
    }

    public void addParameter(VariableEntity entity) {
        functionScope.addParameter(entity);
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
}

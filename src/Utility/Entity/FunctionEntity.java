package Utility.Entity;

import Utility.Cursor;
import Utility.Scope.FunctionScope;
import Utility.Type.Type;


public class FunctionEntity extends MethodEntity {
    private Type returnType;
    private FunctionScope functionScope;

    public FunctionEntity(FunctionScope functionScope, String entityName, Cursor cursor) {
        super(entityName, cursor);
        this.returnType = functionScope.getReturnType();
        this.functionScope = functionScope;
    }

    public void addParameter(VariableEntity entity) {
        functionScope.addParameter(entity);
    }

    public VariableEntity getParameter(int index) {
        return functionScope.getParameter(index);
    }

    public Type getReturnType() {
        return returnType;
    }

    public FunctionScope getFunctionScope() {
        return functionScope;
    }
}

package Utility.Entity;

import Utility.Cursor;
import Utility.Scope.FunctionScope;
import Utility.Type.Type;


public class FunctionEntity extends MethodEntity {
    private Type returnType;
    private FunctionScope functionScope;

    public FunctionEntity(FunctionScope functionScope, Type returnType, String entityName, Cursor cursor) {
        super(entityName, cursor);
        this.returnType = returnType;
        this.functionScope = functionScope;
    }

    public void addParameter(VariableEntity entity) {
        functionScope.addParameter(entity);
    }

    public Type getReturnType() {
        return returnType;
    }

    public FunctionScope getFunctionScope() {
        return functionScope;
    }
}

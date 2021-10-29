package Utility.Entity;

import Utility.Cursor;
import Utility.Scope.FunctionScope;
import Utility.Type.Type;


public class FunctionEntity extends MethodEntity {
    private Type returnType;
    private FunctionScope functionScope;

    public FunctionEntity(Type returnType, String entityName, Cursor cursor) {
        super(entityName, cursor);
        this.returnType = returnType;
    }

    public void addParameter(VariableEntity entity) {
        functionScope.addParameter(entity);
    }

    public void setFunctionScope(FunctionScope functionScope) {
        this.functionScope = functionScope;
    }
}

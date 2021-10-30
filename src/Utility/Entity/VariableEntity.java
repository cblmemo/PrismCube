package Utility.Entity;

import Utility.Cursor;
import Utility.Type.Type;

public class VariableEntity extends Entity {
    private Type variableType;

    public VariableEntity(Type variableType, String entityName, Cursor cursor) {
        super(entityName, cursor);
        this.variableType = variableType;
    }

    public Type getVariableType() {
        return variableType;
    }
}

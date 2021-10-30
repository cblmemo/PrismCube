package Utility.Entity;

import Utility.Cursor;
import Utility.Scope.ConstructorScope;

public class ConstructorEntity extends MethodEntity {
    private ConstructorScope constructorScope;

    public ConstructorEntity(String entityName, Cursor cursor) {
        super(entityName, cursor);
    }

    public void setConstructorScope(ConstructorScope constructorScope) {
        this.constructorScope = constructorScope;
    }

    public ConstructorScope getConstructorScope() {
        return constructorScope;
    }
}

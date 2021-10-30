package Utility.Scope;

import Utility.Entity.ConstructorEntity;

public class ClassScope extends Scope {
    private ConstructorEntity constructor = null;

    public ClassScope(Scope parentScope) {
        super(parentScope);
    }

    public void setConstructor(ConstructorEntity constructor) {
        this.constructor = constructor;
    }

    public boolean hasCustomConstructor() {
        return constructor != null;
    }

    public ConstructorEntity getConstructor() {
        return constructor;
    }
}

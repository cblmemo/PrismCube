package Utility.Scope;

import Utility.Entity.ConstructorEntity;

public class ClassScope extends Scope {
    private ConstructorEntity constructor = null;
    private String className;

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

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}

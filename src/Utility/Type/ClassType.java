package Utility.Type;

import Utility.Entity.*;
import Utility.Scope.ClassScope;

import java.util.Objects;

public class ClassType extends Type {
    private ClassScope classScope;

    public ClassType(String typeName) {
        super(typeName);
    }

    public void setClassScope(ClassScope classScope) {
        this.classScope = classScope;
        this.classScope.setClassName(getTypeName());
    }

    public void addMember(VariableEntity entity) {
        classScope.addVariable(entity);
    }

    public void addMethod(MethodEntity entity) {
        if (entity instanceof ConstructorEntity) classScope.setConstructor((ConstructorEntity) entity);
        else classScope.addFunction((FunctionEntity) entity);
    }

    public ClassScope getClassScope() {
        return classScope;
    }

    public boolean isBuiltinType() {
        return classScope == null || Objects.equals(getTypeName(), "string");
    }
}

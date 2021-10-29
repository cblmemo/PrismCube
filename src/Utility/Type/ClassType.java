package Utility.Type;

import Utility.Entity.*;
import Utility.Scope.ClassScope;

public class ClassType extends Type {
    private ClassScope classScope;

    public ClassType(String typeName) {
        super(typeName);
    }

    public void setClassScope(ClassScope classScope) {
        this.classScope = classScope;
    }

    public void addMember(VariableEntity entity) {
        classScope.addVariable(entity);
    }

    public void addMethod(MethodEntity entity) {
        if (entity instanceof ConstructorEntity) classScope.setConstructor((ConstructorEntity) entity);
        else classScope.addFunction((FunctionEntity) entity);
    }
}

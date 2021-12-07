package Utility.Type;

import IR.TypeSystem.IRStructureType;
import Utility.Cursor;
import Utility.Entity.ConstructorEntity;
import Utility.Entity.FunctionEntity;
import Utility.Entity.MethodEntity;
import Utility.Entity.VariableEntity;
import Utility.Scope.ClassScope;
import Utility.Scope.GlobalScope;
import Utility.error.SemanticError;

import java.util.Objects;

public class ClassType extends Type {
    private ClassScope classScope;
    private IRStructureType classIRType;

    public ClassType(String typeName) {
        super(typeName);
    }

    public void setClassScope(ClassScope classScope) {
        this.classScope = classScope;
        this.classScope.setClassName(getTypeName());
    }

    public void addMember(VariableEntity entity) {
        entity.setAsClassMember();
        classScope.addVariable(entity);
    }

    public void addMethod(MethodEntity entity) {
        if (entity instanceof ConstructorEntity) classScope.setConstructor((ConstructorEntity) entity);
        else classScope.addFunction((FunctionEntity) entity);
    }

    public ClassScope getClassScope() {
        return classScope;
    }

    public void setClassIRType(IRStructureType classIRType) {
        this.classIRType = classIRType;
    }

    public IRStructureType getClassIRType() {
        return classIRType;
    }

    public boolean isBuiltinType() {
        return classScope == null || Objects.equals(getTypeName(), "string");
    }

    public ArrayType toArrayType(int dimension, GlobalScope globalScope) {
        ClassType rootElementType = globalScope.getClass(getTypeName());
        if (rootElementType == null) throw new SemanticError("root element type doesn't exist in global scope", new Cursor(-100, -100));
        return new ArrayType(rootElementType, dimension);
    }
}

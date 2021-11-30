package Utility.Scope;

import IR.Operand.IRRegister;
import Utility.Entity.VariableEntity;
import Utility.Type.Type;
import Utility.error.SemanticError;

import java.util.ArrayList;
import java.util.Objects;

public class FunctionScope extends MethodScope {
    private final ArrayList<VariableEntity> parameters = new ArrayList<>();
    private Type returnType;
    private boolean isLambdaScope = false;

    // for ir
    private IRRegister returnValuePtr;
    private boolean hasReturnStatement = false;

    public FunctionScope(Type returnType, Scope parentScope) {
        super(parentScope);
        this.returnType = returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public Type getReturnType() {
        return returnType;
    }

    public boolean hasParameter(String name) {
        for (var parameter : parameters) {
            if (Objects.equals(parameter.getEntityName(), name)) return true;
        }
        return false;
    }

    public void addParameter(VariableEntity entity) {
        if (hasParameter(entity.getEntityName()))
            throw new SemanticError("repeated parameter name", entity.getCursor());
        parameters.add(entity);
    }

    public ArrayList<VariableEntity> getParameters() {
        return parameters;
    }

    public int getParameterNumber() {
        return parameters.size();
    }

    public VariableEntity getParameter(int index) {
        return parameters.get(index);
    }

    public VariableEntity getParameter(String name) {
        for (var parameter : parameters) {
            if (Objects.equals(parameter.getEntityName(), name)) return parameter;
        }
        return null;
    }

    public Type getParameterType(String name) {
        for (var parameter : parameters) {
            if (Objects.equals(parameter.getEntityName(), name)) return parameter.getVariableType();
        }
        return null;
    }

    public void setLambdaScope() {
        isLambdaScope = true;
    }

    public boolean isLambdaScope() {
        return isLambdaScope;
    }

    // for ir

    public void setReturnValuePtr(IRRegister returnValuePtr) {
        this.returnValuePtr = returnValuePtr;
    }

    public boolean hasReturnStatement() {
        return hasReturnStatement;
    }

    @Override
    public IRRegister getReturnValuePtr() {
        hasReturnStatement = true;
        return returnValuePtr;
    }
}

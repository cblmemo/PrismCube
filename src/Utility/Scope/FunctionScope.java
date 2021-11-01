package Utility.Scope;

import Utility.Entity.VariableEntity;
import Utility.Type.Type;
import Utility.error.SemanticError;

import java.util.ArrayList;
import java.util.Objects;

public class FunctionScope extends MethodScope {
    private ArrayList<VariableEntity> parameters = new ArrayList<>();
    private Type returnType;

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

    public VariableEntity getParameter(int index) {
        return parameters.get(index);
    }

    public Type getParameterType(String name) {
        for (var parameter : parameters) {
            if (Objects.equals(parameter.getEntityName(), name)) return parameter.getVariableType();
        }
        return null;
    }
}

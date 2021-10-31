package Utility.Scope;

import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.Type.Type;
import Utility.error.SemanticError;

import java.util.HashMap;

abstract public class Scope {
    private HashMap<String, VariableEntity> variables = new HashMap<>();
    private HashMap<String, FunctionEntity> functions = new HashMap<>();
    private Scope parentScope;

    public Scope(Scope parentScope) {
        this.parentScope = parentScope;
    }

    public void addVariable(VariableEntity entity) {
        if (variables.containsKey(entity.getEntityName()))
            throw new SemanticError("repeated variable name", entity.getCursor());
        variables.put(entity.getEntityName(), entity);
    }

    public void addFunction(FunctionEntity entity) {
        if (functions.containsKey(entity.getEntityName()))
            throw new SemanticError("repeated function name", entity.getCursor());
        functions.put(entity.getEntityName(), entity);
    }

    public Scope getParentScope() {
        return parentScope;
    }

    public HashMap<String, VariableEntity> getVariables() {
        return variables;
    }

    public HashMap<String, FunctionEntity> getFunctions() {
        return functions;
    }

    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    public boolean hasVariableRecursively(String name) {
        if (hasVariable(name)) return true;
        if (parentScope != null) return parentScope.hasVariableRecursively(name);
        return false;
    }

    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    public boolean hasFunctionRecursively(String name) {
        if (hasFunction(name)) return true;
        if (parentScope != null) return parentScope.hasFunctionRecursively(name);
        return false;
    }

    public boolean hasIdentifier(String name) {
        return hasVariable(name) || hasFunction(name);
    }

    public boolean hasIdentifierRecursively(String name) {
        if (hasIdentifier(name)) return true;
        if (parentScope != null) return parentScope.hasIdentifierRecursively(name);
        return false;
    }

    public Type getVariableType(String name) {
        if (!hasVariable(name)) return null;
        return variables.get(name).getVariableType();
    }

    public Type getVariableTypeRecursively(String name) {
        if (hasVariable(name)) return variables.get(name).getVariableType();
        if (parentScope != null) return parentScope.getVariableTypeRecursively(name);
        return null;
    }

    public Type getFunctionReturnType(String name) {
        if (!hasFunction(name)) return null;
        return functions.get(name).getReturnType();
    }

    public Type getFunctionReturnTypeRecursively(String name) {
        if (hasFunction(name)) return functions.get(name).getReturnType();
        if (parentScope != null) return parentScope.getFunctionReturnTypeRecursively(name);
        return null;
    }

    public VariableEntity getVariable(String name) {
        return variables.get(name);
    }

    public VariableEntity getVariableRecursively(String name) {
        if (variables.containsKey(name)) return variables.get(name);
        if (parentScope != null) return parentScope.getVariableRecursively(name);
        return null;
    }

    public FunctionEntity getFunction(String name) {
        return functions.get(name);
    }

    public FunctionEntity getFunctionRecursively(String name) {
        if (functions.containsKey(name)) return functions.get(name);
        if (parentScope != null) return parentScope.getFunctionRecursively(name);
        return null;
    }

    public boolean insideMethod() {
        if (parentScope == null) return false;
        if (this instanceof MethodScope && parentScope instanceof ClassScope) return true;
        return parentScope.insideMethod();
    }
}

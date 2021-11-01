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
        if (this instanceof FunctionScope && ((FunctionScope) this).hasParameter(name)) return true;
        return variables.containsKey(name);
    }

    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    public boolean hasIdentifier(String name) {
        return hasVariable(name) || hasFunction(name);
    }

    public boolean hasIdentifierRecursively(String name) {
        if (this instanceof FunctionScope && ((FunctionScope) this).hasParameter(name)) return true;
        if (hasIdentifier(name)) return true;
        if (parentScope != null) return parentScope.hasIdentifierRecursively(name);
        return false;
    }

    public Type getVariableType(String name) {
        if (this instanceof FunctionScope && ((FunctionScope) this).hasParameter(name))
            return ((FunctionScope) this).getParameterType(name);
        if (!hasVariable(name)) return null;
        return variables.get(name).getVariableType();
    }

    public Type getVariableTypeRecursively(String name) {
        if (hasVariable(name)) return getVariableType(name);
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

    public FunctionEntity getFunction(String name) {
        return functions.get(name);
    }

    public FunctionEntity getFunctionRecursively(String name) {
        if (functions.containsKey(name)) return getFunction(name);
        if (parentScope != null) return parentScope.getFunctionRecursively(name);
        return null;
    }

    public boolean insideClassMethod() {
        if (parentScope == null) return false;
        if (this instanceof MethodScope && parentScope instanceof ClassScope) return true;
        return parentScope.insideClassMethod();
    }

    public boolean insideLoop() {
        if (this instanceof LoopScope) return true;
        if (parentScope != null && (this instanceof BracesScope || this instanceof BranchScope)) return parentScope.insideLoop();
        return false;
    }

    public ClassScope getUpperClassScope() {
        if (parentScope == null) return null;
        if (this instanceof MethodScope && parentScope instanceof ClassScope) return (ClassScope) parentScope;
        return parentScope.getUpperClassScope();
    }

    public boolean insideMethod() {
        if (this instanceof MethodScope) return true;
        if (parentScope != null) return parentScope.insideMethod();
        return false;
    }

    public MethodScope getMethodScope() {
        if (!insideMethod()) return null;
        if (this instanceof MethodScope) return (MethodScope) this;
        if (parentScope != null) return parentScope.getMethodScope();
        return null;
    }
}

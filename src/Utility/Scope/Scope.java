package Utility.Scope;

import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.error.SemanticError;

import java.util.HashMap;

public class Scope {
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

    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }
}

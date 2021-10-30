package Utility.Scope;

import Utility.Entity.VariableEntity;
import Utility.error.SemanticError;

import java.util.HashMap;

public class FunctionScope extends Scope {
    private HashMap<String, VariableEntity> parameters = new HashMap<>();

    public FunctionScope(Scope parentScope) {
        super(parentScope);
    }

    public void addParameter(VariableEntity entity) {
        if (parameters.containsKey(entity.getEntityName()))
            throw new SemanticError("repeated parameter name", entity.getCursor());
        parameters.put(entity.getEntityName(), entity);
    }

    public HashMap<String, VariableEntity> getParameters() {
        return parameters;
    }
}

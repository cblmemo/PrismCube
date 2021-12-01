package Utility.Scope;

import AST.StatementNode.StatementNode;
import IR.Operand.IRRegister;
import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.Type.Type;
import Utility.error.SemanticError;

import java.util.HashMap;

abstract public class Scope {
    private final HashMap<String, VariableEntity> variables = new HashMap<>();
    private final HashMap<String, FunctionEntity> functions = new HashMap<>();
    private final Scope parentScope;
    private int blockScopeCnt = 0;
    private final HashMap<Integer, BlockScope> blockScopes = new HashMap<>();

    // for ir
    private boolean encounteredFlow = false;

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

    public BlockScope getBlockScope(int scopeId) {
        assert blockScopes.containsKey(scopeId);
        return blockScopes.get(scopeId);
    }

    public BracesScope createBracesScope(StatementNode node) {
        node.setScopeId(++blockScopeCnt);
        BracesScope scope = new BracesScope(this);
        blockScopes.put(blockScopeCnt, scope);
        return scope;
    }

    public BranchScope createBranchScope(StatementNode node) {
        node.setScopeId(++blockScopeCnt);
        BranchScope scope = new BranchScope(this);
        blockScopes.put(blockScopeCnt, scope);
        return scope;
    }

    public LoopScope createLoopScope(StatementNode node) {
        node.setScopeId(++blockScopeCnt);
        LoopScope scope = new LoopScope(this);
        blockScopes.put(blockScopeCnt, scope);
        return scope;
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

    // for ir

    public VariableEntity getVariableEntity(String name) {
        if (this instanceof FunctionScope && ((FunctionScope) this).hasParameter(name))
            return ((FunctionScope) this).getParameter(name);
        if (!hasVariable(name)) return null;
        return variables.get(name);
    }

    public VariableEntity getVariableEntityRecursively(String name) {
        if (hasVariable(name)) return getVariableEntity(name);
        if (parentScope != null) return parentScope.getVariableEntityRecursively(name);
        return null;
    }

    public IRRegister getReturnValuePtr() {
        if (this instanceof FunctionScope)
            return this.getReturnValuePtr();
        assert parentScope != null;
        return parentScope.getReturnValuePtr();
    }

    // a return statement will terminate all statement's translate
    // after it in a same scope.
    public boolean hasEncounteredFlow() {
        return encounteredFlow;
    }

    public void setAsEncounteredFlow() {
        encounteredFlow = true;
    }

    public LoopScope getLoopScope() {
        assert insideLoop();
        if (this instanceof LoopScope) return (LoopScope) this;
        return parentScope.getLoopScope();
    }
}

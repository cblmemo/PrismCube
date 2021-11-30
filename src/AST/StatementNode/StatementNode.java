package AST.StatementNode;

import AST.ASTNode;
import Utility.Cursor;

public abstract class StatementNode extends ASTNode {
    private int scopeId = -1;
    private int ifElseId = -1;

    public StatementNode(Cursor cursor) {
        super(cursor);
    }

    public void setScopeId(int scopeId) {
        if (this.scopeId == -1) this.scopeId = scopeId;
        else this.ifElseId = scopeId;
    }

    public int getIfElseId() {
        return ifElseId;
    }

    public int getScopeId() {
        return scopeId;
    }
}

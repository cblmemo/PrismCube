package AST.StatementNode;

import AST.ASTNode;
import Utility.Cursor;

public abstract class StatementNode extends ASTNode {
    private int scopeId = -1;

    public StatementNode(Cursor cursor) {
        super(cursor);
    }

    public void setScopeId(int scopeId) {
        this.scopeId = scopeId;
    }

    public int getScopeId() {
        return scopeId;
    }
}

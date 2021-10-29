package AST.StatementNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class ContinueStatementNode extends StatementNode {
    public ContinueStatementNode(Cursor cursor) {
        super(cursor);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

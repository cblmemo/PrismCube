package AST.StatementNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class BreakStatementNode extends StatementNode {
    public BreakStatementNode(Cursor cursor) {
        super(cursor);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

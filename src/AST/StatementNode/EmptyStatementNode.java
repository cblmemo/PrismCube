package AST.StatementNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class EmptyStatementNode extends StatementNode {
    public EmptyStatementNode(Cursor cursor) {
        super(cursor);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

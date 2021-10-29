package AST.StatementNode;

import AST.ASTVisitor;
import Utility.Cursor;

import java.util.ArrayList;

public class BlockStatementNode extends StatementNode {
    private ArrayList<StatementNode> statements = new ArrayList<>();

    public BlockStatementNode(Cursor cursor) {
        super(cursor);
    }

    public void addStatement(StatementNode node) {
        statements.add(node);
    }

    public ArrayList<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

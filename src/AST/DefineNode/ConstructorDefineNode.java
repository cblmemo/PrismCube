package AST.DefineNode;

import AST.ASTVisitor;
import AST.PrimaryNode.IdentifierPrimaryNode;
import AST.StatementNode.StatementNode;
import Utility.Cursor;

import java.util.ArrayList;

public class ConstructorDefineNode extends ProgramDefineNode {
    private IdentifierPrimaryNode constructorName;
    private ArrayList<StatementNode> statements = new ArrayList<>();

    public ConstructorDefineNode(IdentifierPrimaryNode constructorName, Cursor cursor) {
        super(cursor);
        this.constructorName = constructorName;
    }

    public String getConstructorName() {
        return constructorName.getIdentifier();
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

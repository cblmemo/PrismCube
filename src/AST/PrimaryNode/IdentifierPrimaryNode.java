package AST.PrimaryNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class IdentifierPrimaryNode extends PrimaryNode {
    private String identifier;

    public IdentifierPrimaryNode(String identifier, Cursor cursor) {
        super(cursor);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST.TypeNode;

import AST.ASTVisitor;
import AST.PrimaryNode.IdentifierPrimaryNode;
import Utility.Cursor;

public class ClassTypeNode extends TypeNode {
    private IdentifierPrimaryNode className;

    public ClassTypeNode(IdentifierPrimaryNode className, Cursor cursor) {
        super(className.getIdentifier(), cursor);
        this.className = className;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

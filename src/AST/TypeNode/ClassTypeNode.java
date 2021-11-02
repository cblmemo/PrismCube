package AST.TypeNode;

import AST.ASTVisitor;
import AST.PrimaryNode.IdentifierPrimaryNode;
import Utility.Cursor;

public class ClassTypeNode extends TypeNode {

    public ClassTypeNode(IdentifierPrimaryNode className, Cursor cursor) {
        super(className.getIdentifier(), cursor);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

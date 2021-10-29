package AST.TypeNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class SpecialTypeNode extends TypeNode {
    public SpecialTypeNode(String typeName, Cursor cursor) {
        super(typeName, cursor);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

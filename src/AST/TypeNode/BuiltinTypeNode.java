package AST.TypeNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class BuiltinTypeNode extends TypeNode {
    public BuiltinTypeNode(String typeName, Cursor cursor) {
        super(typeName, cursor);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

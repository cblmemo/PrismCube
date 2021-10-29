package AST.TypeNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class ReturnTypeNode extends TypeNode {
    public ReturnTypeNode(String typeName, Cursor cursor) {
        super(typeName, cursor);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

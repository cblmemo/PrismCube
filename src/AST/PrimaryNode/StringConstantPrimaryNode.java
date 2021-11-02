package AST.PrimaryNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class StringConstantPrimaryNode extends PrimaryNode {
    private final String stringConstant;

    public StringConstantPrimaryNode(String stringConstantText, Cursor cursor) {
        super(false, cursor);
        this.stringConstant = stringConstantText;
    }

    public String getStringConstant() {
        return stringConstant;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST.PrimaryNode;

import AST.ASTVisitor;
import Utility.Cursor;

import java.util.Objects;

public class BoolConstantPrimaryNode extends PrimaryNode {
    private boolean boolConstant;

    public BoolConstantPrimaryNode(String boolConstantText, Cursor cursor) {
        super(false, cursor);
        this.boolConstant = Objects.equals(boolConstantText, "true");
    }

    public boolean getBoolConstant() {
        return boolConstant;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

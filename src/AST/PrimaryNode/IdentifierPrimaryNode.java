package AST.PrimaryNode;

import AST.ASTVisitor;
import Utility.Cursor;

public class IdentifierPrimaryNode extends PrimaryNode {
    private final String identifier;
    private final boolean isVariable;
    private final boolean isFunction;

    public IdentifierPrimaryNode(String identifier, boolean isVariable, boolean isFunction, Cursor cursor) {
        super(isVariable, cursor);
        this.identifier = identifier;
        this.isVariable = isVariable;
        this.isFunction = isFunction;
    }

    public boolean isVariable() {
        return isVariable;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

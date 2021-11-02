package AST.DefineNode;

import AST.ASTVisitor;
import AST.PrimaryNode.IdentifierPrimaryNode;
import AST.TypeNode.TypeNode;
import Utility.Cursor;

public class ParameterDefineNode extends ProgramDefineNode {
    private final TypeNode type;
    private final IdentifierPrimaryNode parameterName;

    public ParameterDefineNode(TypeNode type, IdentifierPrimaryNode parameterName, Cursor cursor) {
        super(cursor);
        this.type = type;
        this.parameterName = parameterName;
    }

    public TypeNode getType() {
        return type;
    }

    public String getParameterName() {
        return parameterName.getIdentifier();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

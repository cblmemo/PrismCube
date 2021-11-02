package AST.DefineNode;

import AST.ASTVisitor;
import AST.ExpressionNode.ExpressionNode;
import AST.PrimaryNode.IdentifierPrimaryNode;
import AST.TypeNode.TypeNode;
import Utility.Cursor;

public class SingleVariableDefineNode extends ProgramDefineNode {
    private TypeNode type;
    private final IdentifierPrimaryNode variableName;
    private final ExpressionNode initializeValue;

    public SingleVariableDefineNode(IdentifierPrimaryNode variableName, ExpressionNode initializeValue, Cursor cursor) {
        super(cursor);
        this.variableName = variableName;
        this.initializeValue = initializeValue;
    }

    public void setType(TypeNode type) {
        this.type = type;
    }

    public TypeNode getType() {
        return type;
    }

    public String getVariableNameStr() {
        return variableName.getIdentifier();
    }

    public IdentifierPrimaryNode getVariableName() {
        return variableName;
    }

    public ExpressionNode getInitializeValue() {
        return initializeValue;
    }

    public boolean hasInitializeValue() {
        return initializeValue != null;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

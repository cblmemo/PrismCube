package AST.ExpressionNode;

import AST.ASTVisitor;
import AST.TypeNode.TypeNode;
import Utility.Cursor;

import java.util.ArrayList;

public class NewTypeExpressionNode extends ExpressionNode {
    private TypeNode nonArrayType;
    private int dimension = 0;
    private ArrayList<ExpressionNode> dimensionExpressions = new ArrayList<>();
    private boolean invalid;

    public NewTypeExpressionNode(TypeNode nonArrayType, boolean invalid, Cursor cursor) {
        super(cursor);
        this.nonArrayType = nonArrayType;
        this.invalid = invalid;
    }

    public void addDimensionExpression(ExpressionNode node) {
        dimensionExpressions.add(node);
    }

    public void increaseDimension() {
        dimension++;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public TypeNode getNonArrayType() {
        return nonArrayType;
    }

    public int getDimension() {
        return dimension;
    }

    public ArrayList<ExpressionNode> getDimensionExpressions() {
        return dimensionExpressions;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

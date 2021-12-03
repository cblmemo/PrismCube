package AST.ExpressionNode;

import AST.ASTVisitor;
import AST.TypeNode.TypeNode;
import Utility.Cursor;

import java.util.ArrayList;

public class NewTypeExpressionNode extends ExpressionNode {
    private final TypeNode rootElementType;
    private int dimension = 0;
    private final ArrayList<ExpressionNode> dimensionExpressions = new ArrayList<>();
    private final boolean invalid;

    public NewTypeExpressionNode(TypeNode rootElementType, boolean invalid, Cursor cursor) {
        super(false, cursor);
        this.rootElementType = rootElementType;
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

    public boolean isNewArray() {
        return dimension != 0;
    }

    public TypeNode getRootElementType() {
        return rootElementType;
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

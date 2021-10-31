package AST.ExpressionNode;

import AST.ASTNode;
import Utility.Cursor;
import Utility.Type.Type;

public abstract class ExpressionNode extends ASTNode {
    private Type expressionType;
    private boolean leftValue;

    public ExpressionNode(boolean leftValue, Cursor cursor) {
        super(cursor);
        this.leftValue = leftValue;
    }

    public Type getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(Type expressionType) {
        this.expressionType = expressionType;
    }

    public boolean isLeftValue() {
        return leftValue;
    }

    public void setLeftValue(boolean leftValue) {
        this.leftValue = leftValue;
    }
}

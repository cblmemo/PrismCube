package AST.StatementNode;

import AST.ASTVisitor;
import AST.ExpressionNode.ExpressionNode;
import Utility.Cursor;

public class ForStatementNode extends StatementNode { // todo check whether have
    private ExpressionNode initializeExpression = null, conditionExpression = null, stepExpression = null;
    private StatementNode loopBody;

    public ForStatementNode(StatementNode loopBody, Cursor cursor) {
        super(cursor);
        this.loopBody = loopBody;
    }

    public void setInitializeExpression(ExpressionNode initializeExpression) {
        this.initializeExpression = initializeExpression;
    }

    public void setConditionExpression(ExpressionNode conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public void setStepExpression(ExpressionNode stepExpression) {
        this.stepExpression = stepExpression;
    }

    public boolean hasInitializeExpression() {
        return initializeExpression != null;
    }

    public boolean hasConditionExpression() {
        return conditionExpression != null;
    }

    public boolean hasStepExpression() {
        return stepExpression != null;
    }

    public ExpressionNode getInitializeExpression() {
        return initializeExpression;
    }

    public ExpressionNode getConditionExpression() {
        return conditionExpression;
    }

    public ExpressionNode getStepExpression() {
        return stepExpression;
    }

    public StatementNode getLoopBody() {
        return loopBody;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

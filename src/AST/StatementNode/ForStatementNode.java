package AST.StatementNode;

import AST.ASTNode;
import AST.ASTVisitor;
import AST.ExpressionNode.ExpressionNode;
import Utility.Cursor;

public class ForStatementNode extends StatementNode {
    private ASTNode initializeStatement = null;
    private ExpressionNode conditionExpression = null, stepExpression = null;
    private final StatementNode loopBody;

    public ForStatementNode(StatementNode loopBody, Cursor cursor) {
        super(cursor);
        this.loopBody = loopBody;
    }

    public void setInitializeStatement(ASTNode initializeStatement) {
        this.initializeStatement = initializeStatement;
    }

    public void setConditionExpression(ExpressionNode conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public void setStepExpression(ExpressionNode stepExpression) {
        this.stepExpression = stepExpression;
    }

    public boolean hasInitializeStatement() {
        return initializeStatement != null;
    }

    public boolean hasConditionExpression() {
        return conditionExpression != null;
    }

    public boolean hasStepExpression() {
        return stepExpression != null;
    }

    public ASTNode getInitializeStatement() {
        return initializeStatement;
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

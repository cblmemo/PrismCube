package AST.StatementNode;

import AST.ASTVisitor;
import AST.ExpressionNode.ExpressionNode;
import Utility.Cursor;

public class ReturnStatementNode extends StatementNode {
    private ExpressionNode returnValue;

    public ReturnStatementNode(Cursor cursor) {
        super(cursor);
    }

    public void setReturnValue(ExpressionNode returnValue) {
        this.returnValue = returnValue;
    }

    public ExpressionNode getReturnValue() {
        return returnValue;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

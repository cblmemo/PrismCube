package AST.ExpressionNode;

import AST.ASTVisitor;
import AST.PrimaryNode.IdentifierPrimaryNode;
import Utility.Cursor;

public class MemberAccessExpressionNode extends ExpressionNode {
    private ExpressionNode instance;
    private IdentifierPrimaryNode memberName;

    public MemberAccessExpressionNode(ExpressionNode instance, IdentifierPrimaryNode memberName, Cursor cursor) {
        super(cursor);
        this.instance = instance;
        this.memberName = memberName;
    }

    public ExpressionNode getInstance() {
        return instance;
    }

    public IdentifierPrimaryNode getMemberName() {
        return memberName;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

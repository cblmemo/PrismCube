package AST.ExpressionNode;

import AST.ASTVisitor;
import AST.PrimaryNode.IdentifierPrimaryNode;
import Utility.Cursor;

public class MemberAccessExpressionNode extends LeftValueExpressionNode {
    private final ExpressionNode instance;
    private final IdentifierPrimaryNode memberName;
    private boolean accessMethod = false;

    public MemberAccessExpressionNode(ExpressionNode instance, IdentifierPrimaryNode memberName, Cursor cursor) {
        super(true, cursor);
        this.instance = instance;
        this.memberName = memberName;
    }

    public ExpressionNode getInstance() {
        return instance;
    }

    public String getMemberName() {
        return memberName.getIdentifier();
    }

    public void setAccessMethod(boolean accessMethod) {
        this.accessMethod = accessMethod;
        super.setLeftValue(!accessMethod);
    }

    public boolean isAccessMethod() {
        return accessMethod;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

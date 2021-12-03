package AST.ExpressionNode;

import AST.ASTNode;
import AST.PrimaryNode.IdentifierPrimaryNode;
import IR.Operand.IROperand;
import Utility.ConstExpr.ConstExprEntry;
import Utility.Cursor;
import Utility.Type.Type;
import Utility.error.IRError;

import java.util.Objects;

public abstract class ExpressionNode extends ASTNode {
    private Type expressionType;
    private boolean leftValue;

    public ExpressionNode(boolean leftValue, Cursor cursor) {
        super(cursor);
        this.leftValue = leftValue;
        this.entry = ConstExprEntry.nonConstExprEntry;
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

    // for ir
    private ConstExprEntry entry;
    private IROperand resultRegister;

    public void setEntry(ConstExprEntry entry) {
        this.entry = entry;
    }

    public ConstExprEntry getEntry() {
        return entry;
    }

    public void setIRResultValue(IROperand resultRegister) {
        this.resultRegister = resultRegister;
    }

    public IROperand getIRResultValue() {
        return resultRegister;
    }

    public ASTNode getBottomLeftValueNode() {
        if (this instanceof IdentifierPrimaryNode) return this;
        if (this instanceof AddressingExpressionNode) return this;
        if (this instanceof MemberAccessExpressionNode) return this;
        if (this instanceof AssignExpressionNode) return ((AssignExpressionNode) this).getLhs().getBottomLeftValueNode();
        if (this instanceof UnaryExpressionNode) {
            assert Objects.equals(((UnaryExpressionNode) this).getOp(), "++") || Objects.equals(((UnaryExpressionNode) this).getOp(), "--");
            return ((UnaryExpressionNode) this).getRhs().getBottomLeftValueNode();
        }
        throw new IRError("cannot get bottom left value node to an un-left-value node");
    }
}

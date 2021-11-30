package AST.ExpressionNode;

import AST.ASTNode;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import Utility.Cursor;
import Utility.Type.Type;
import Utility.ConstExpr.ConstExprEntry;

public abstract class ExpressionNode extends ASTNode {
    private Type expressionType;
    private boolean leftValue;

    // for ir
    private ConstExprEntry entry;
    private IROperand resultRegister;

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
}

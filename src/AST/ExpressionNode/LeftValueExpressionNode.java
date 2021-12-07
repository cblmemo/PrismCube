package AST.ExpressionNode;

import IR.Operand.IRRegister;
import Utility.Cursor;

abstract public class LeftValueExpressionNode extends ExpressionNode {
    public LeftValueExpressionNode(boolean leftValue, Cursor cursor) {
        super(leftValue, cursor);
    }

    // for ir
    private IRRegister leftValuePointer = null;

    public void setLeftValuePointer(IRRegister leftValuePointer) {
        this.leftValuePointer = leftValuePointer;
    }

    public boolean hasLeftValuePointer() {
        return leftValuePointer != null;
    }

    public IRRegister getLeftValuePointer() {
        return leftValuePointer;
    }
}

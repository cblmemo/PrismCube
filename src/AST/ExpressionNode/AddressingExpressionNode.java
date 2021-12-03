package AST.ExpressionNode;

import AST.ASTVisitor;
import IR.Operand.IRRegister;
import Utility.Cursor;

public class AddressingExpressionNode extends ExpressionNode {
    private final ExpressionNode array;
    private final ExpressionNode index;

    public AddressingExpressionNode(ExpressionNode array, ExpressionNode index, Cursor cursor) {
        super(true, cursor);
        this.array = array;
        this.index = index;
    }

    public ExpressionNode getArray() {
        return array;
    }

    public ExpressionNode getIndex() {
        return index;
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

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

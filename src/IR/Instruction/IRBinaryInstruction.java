package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;

public class IRBinaryInstruction extends IRInstruction {
    private final String op;
    private final IRRegister resultRegister;
    private final IROperand lhs;
    private final IROperand rhs;

    public IRBinaryInstruction(String op, IRRegister resultRegister, IROperand lhs, IROperand rhs) {
        this.op = op;
        this.resultRegister = resultRegister;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public String getOp() {
        return op;
    }

    public IRRegister getResultRegister() {
        return resultRegister;
    }

    public IROperand getLhs() {
        return lhs;
    }

    public IROperand getRhs() {
        return rhs;
    }

    @Override
    public String toString() {
        return resultRegister + " = " + op + " " + lhs.getIRType() + " " + lhs + ", " + rhs;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

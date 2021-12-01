package IR.Instruction;

import IR.IRVisitor;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;

public class IRIcmpInstruction extends IRInstruction{
    private final String op;
    private final IRRegister resultRegister;
    private final IROperand lhs;
    private final IROperand rhs;

    public IRIcmpInstruction(String op, IRRegister resultRegister, IROperand lhs, IROperand rhs) {
        this.op = op;
        this.resultRegister = resultRegister;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        return resultRegister + " = icmp " + op + " " + lhs.getIRType() + " " + lhs + ", " + rhs;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

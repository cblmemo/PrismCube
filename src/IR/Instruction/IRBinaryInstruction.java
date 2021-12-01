package IR.Instruction;

import IR.IRVisitor;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;

public class IRBinaryInstruction extends IRInstruction {
    private String op;
    private IRRegister resultRegister;
    private IROperand lhs;
    private IROperand rhs;

    public IRBinaryInstruction(String op, IRRegister resultRegister, IROperand lhs, IROperand rhs) {
        this.op = op;
        this.resultRegister = resultRegister;
        this.lhs = lhs;
        this.rhs = rhs;
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

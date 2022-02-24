package IR.Operand;

import IR.IRBasicBlock;

public class IRLabel extends IROperand {
    private final String name;
    private final IRBasicBlock belongTo;

    public IRLabel(String name, IRBasicBlock belongTo) {
        super(null);
        this.name = name;
        this.belongTo=belongTo;
    }

    public IRBasicBlock belongTo() {
        return belongTo;
    }

    public String toBasicBlockLabel() {
        return name + ":";
    }

    @Override
    public String toString() {
        return "%" + name;
    }
}

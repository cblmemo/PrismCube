package IR.Operand;

import IR.IRBasicBlock;

public class IRLabel extends IROperand {
    private final String name;
    private final IRBasicBlock belongTo;
    private int id = -1;

    private static boolean printName = false;

    public IRLabel(String name, IRBasicBlock belongTo) {
        super(null);
        this.name = name;
        this.belongTo = belongTo;
    }

    public IRBasicBlock belongTo() {
        return belongTo;
    }

    public String toBasicBlockLabel() {
        return printName ? name + ":" : id + ":";
    }

    public static void printLabelName() {
        printName = true;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return printName ? "%" + name : "%" + id;
    }
}

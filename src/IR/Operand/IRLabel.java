package IR.Operand;

public class IRLabel extends IROperand {
    private final String name;

    public IRLabel(String name) {
        super(null);
        this.name = name;
    }

    public String toBasicBlockLabel() {
        return name + ":";
    }

    @Override
    public String toString() {
        return "%" + name;
    }
}

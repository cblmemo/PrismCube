package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

public class IRRegister extends IROperand {
    private int id;
    private final String name;
    private boolean isAlloca = false;

    private static int registerCnt = 0;
    private static int allocaCnt = 0;
    private static boolean printName = false;

    public static void reset() {
        registerCnt = 0;
    }

    public static void resetAlloca() {
        allocaCnt = 0;
    }

    public static void resetTo(int cnt) {
        registerCnt = cnt;
    }

    public IRRegister(IRTypeSystem irType, String name) {
        super(irType);
        this.id = registerCnt++;
        this.name = name;
    }

    public IRRegister(IRTypeSystem irType, String name, boolean isAlloca) {
        super(irType);
        this.id = -(++allocaCnt);
        this.name = name;
        this.isAlloca = isAlloca;
    }

    public String getName() {
        return name + "_" + Math.abs(id);
    }

    public static void printRegisterName() {
        printName = true;
        IRLabel.printLabelName();
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        if (!printName) return "%" + id;
        if (isAlloca) return "%" + name + "_" + (-id);
        assert id >= 0;
        if (name != null && !name.equals("argument")) return "%" + name + "_" + id;
        return "%" + id;
    }
}

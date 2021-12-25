package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

public class IRRegister extends IROperand {
    private final int id;
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

    public static void resetAllocaTo(int cnt) {
        allocaCnt = cnt;
    }

    public static int getCurrentCnt() {
        return registerCnt;
    }

    public IRRegister(IRTypeSystem irType, String name) {
        super(irType);
        this.id = registerCnt++;
        this.name = name;
    }

    public IRRegister(IRTypeSystem irType, int id) {
        super(irType);
        this.id = id;
        this.name = null;
    }

    public IRRegister(IRTypeSystem irType, String name, boolean isAlloca) {
        super(irType);
        this.id = -(++allocaCnt);
        this.name = name;
        this.isAlloca = isAlloca;
    }

    public String getName() {
        return name;
    }

    public static void printRegisterName() {
        printName = true;
    }

    @Override
    public String toString() {
        if (isAlloca) return "%" + name + "_" + (-id);
        assert id >= 0;
        if (printName && name != null) return "%" + name + "_" + id;
        else return "%" + id;
    }
}

package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

public class IRRegister extends IROperand {
    private final int id;
    private String name = null;

    private static int registerCnt = 0;

    public static void reset() {
        registerCnt = 0;
    }

    public static void resetTo(int cnt) {
        registerCnt = cnt;
    }

    public static int getCurrentCnt() {
        return registerCnt;
    }

    public IRRegister(IRTypeSystem irType) {
        super(irType);
        this.id = registerCnt++;
    }

    public IRRegister(IRTypeSystem irType, String name) {
        super(irType);
        this.id = -1;
        this.name = name;
    }

    public IRRegister(IRTypeSystem irType, int id) {
        super(irType);
        this.id = id;
    }

    public int getId() {
        assert id > 0;
        return id;
    }

    @Override
    public String toString() {
        if (name != null) return "%" + name;
        return "%" + id;
    }
}

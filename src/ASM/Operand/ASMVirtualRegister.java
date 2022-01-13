package ASM.Operand;

public class ASMVirtualRegister extends ASMRegister {
    private final String name;

    private static int cnt = 0;

    public ASMVirtualRegister(String name) {
        this.name = name + "_" + cnt++;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "$" + name;
    }
}

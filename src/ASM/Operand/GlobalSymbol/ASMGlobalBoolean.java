package ASM.Operand.GlobalSymbol;

public class ASMGlobalBoolean extends ASMGlobalSymbol {
    // all global boolean will be initialized by an initialize function
    private final boolean value = false;

    public ASMGlobalBoolean(String symbolName) {
        super(symbolName);
    }

    public boolean getValue() {
        return value;
    }
}

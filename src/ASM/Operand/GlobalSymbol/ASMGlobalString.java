package ASM.Operand.GlobalSymbol;

public class ASMGlobalString extends ASMGlobalSymbol {
    // all global integer will be initialized by an initialize function
    private final int value = 0;

    public ASMGlobalString(String symbolName) {
        super(symbolName);
    }

    public int getValue() {
        return value;
    }
}

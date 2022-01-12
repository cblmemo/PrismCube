package ASM.Operand.GlobalSymbol;

public class ASMGlobalInteger extends ASMGlobalSymbol {
    // all global integer will be initialized by an initialize function
    private final int value = 0;

    public ASMGlobalInteger(String symbolName) {
        super(symbolName);
    }

    public int getValue() {
        return value;
    }
}

package ASM.Operand.GlobalSymbol;

import ASM.Operand.ASMOperand;

abstract public class ASMGlobalSymbol extends ASMOperand {
    private final String symbolName;

    public ASMGlobalSymbol(String symbolName) {
        this.symbolName = symbolName;
    }

    public String getSymbolName() {
        return symbolName;
    }

    abstract public int getValue();

    @Override
    public String toString() {
        return symbolName;
    }
}

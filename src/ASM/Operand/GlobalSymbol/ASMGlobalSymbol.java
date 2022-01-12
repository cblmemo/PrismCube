package ASM.Operand.GlobalSymbol;

import ASM.Operand.ASMOperand;
import Utility.error.ASMError;

abstract public class ASMGlobalSymbol extends ASMOperand {
    private final String symbolName;

    public ASMGlobalSymbol(String symbolName) {
        this.symbolName = symbolName;
    }

    public String getSymbolName() {
        return symbolName;
    }

    @Override
    public String toString() {
        return symbolName;
    }
}

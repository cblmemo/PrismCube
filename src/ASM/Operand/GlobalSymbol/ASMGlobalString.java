package ASM.Operand.GlobalSymbol;

import IR.IRGlobalDefine;
import IR.Operand.IRConstString;
import IR.Operand.IRNull;

public class ASMGlobalString extends ASMGlobalSymbol {
    // all global integer will be initialized by an initialize function
    private final int id;

    public ASMGlobalString(String symbolName, IRGlobalDefine define) {
        super(symbolName);
        assert define.getInitValue() instanceof IRConstString || define.getInitValue() instanceof IRNull;
        if (define.getInitValue() instanceof IRConstString) this.id = ((IRConstString) define.getInitValue()).getId();
        else this.id = -1;
    }

    @Override
    public String getValue() {
        // @see InstructionSelector.getIRConstStringName
        if (id < 0) return "0";
        return ".L.str." + id;
    }

    @Override
    public String getHexValue() {
        assert false;
        return null;
    }
}

package ASM;

import ASM.Operand.ASMConstString;
import ASM.Operand.GlobalSymbol.ASMGlobalSymbol;

import java.util.LinkedHashMap;

public class ASMModule {
    private final LinkedHashMap<String, ASMFunction> builtinFunctions = new LinkedHashMap<>();
    private final LinkedHashMap<String, ASMFunction> functions = new LinkedHashMap<>();
    private final LinkedHashMap<String, ASMGlobalSymbol> globals = new LinkedHashMap<>();
    private final LinkedHashMap<String, ASMConstString> strings = new LinkedHashMap<>();

    public LinkedHashMap<String, ASMFunction> getFunctions() {
        return functions;
    }

    public LinkedHashMap<String, ASMFunction> getBuiltinFunctions() {
        return builtinFunctions;
    }

    public void addGlobal(String name, ASMGlobalSymbol symbol) {
        globals.put(name, symbol);
    }

    public ASMGlobalSymbol getGlobal(String name) {
        assert globals.containsKey(name);
        return globals.get(name);
    }

    public ASMConstString getConstString(String name){
        assert strings.containsKey(name);
        return strings.get(name);
    }

    public void addConstString(String name, ASMConstString string){
        strings.put(name, string);
    }

    public LinkedHashMap<String, ASMGlobalSymbol> getGlobals() {
        return globals;
    }

    public LinkedHashMap<String, ASMConstString> getStrings() {
        return strings;
    }
}

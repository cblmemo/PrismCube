package ASM;

import ASM.Operand.ASMConstString;
import ASM.Operand.GlobalSymbol.ASMGlobalSymbol;

import java.util.HashMap;

public class ASMModule {
    private final HashMap<String, ASMFunction> builtinFunctions = new HashMap<>();
    private final HashMap<String, ASMFunction> functions = new HashMap<>();
    private final HashMap<String, ASMGlobalSymbol> globals = new HashMap<>();
    private final HashMap<String, ASMConstString> strings = new HashMap<>();

    public HashMap<String, ASMFunction> getFunctions() {
        return functions;
    }

    public HashMap<String, ASMFunction> getBuiltinFunctions() {
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

    public HashMap<String, ASMGlobalSymbol> getGlobals() {
        return globals;
    }

    public HashMap<String, ASMConstString> getStrings() {
        return strings;
    }
}

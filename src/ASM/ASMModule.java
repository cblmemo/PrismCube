package ASM;

import java.util.HashMap;

public class ASMModule {
    private HashMap<String, ASMFunction> builtinFunctions = new HashMap<>();
    private HashMap<String, ASMFunction> functions = new HashMap<>();

    public HashMap<String, ASMFunction> getFunctions() {
        return functions;
    }

    public HashMap<String, ASMFunction> getBuiltinFunctions() {
        return builtinFunctions;
    }
}

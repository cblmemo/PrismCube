package ASM;

import java.util.HashMap;

public class ASMModule {
    private HashMap<String, ASMFunction> builtinFunctions = new HashMap<>();
    private HashMap<String, ASMFunction> functions = new HashMap<>();

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}

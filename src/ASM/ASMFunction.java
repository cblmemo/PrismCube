package ASM;

import java.util.ArrayList;

public class ASMFunction {
    private String functionName;
    private final ArrayList<ASMBasicBlock> blocks = new ArrayList<>();
    private final ASMBasicBlock entryBlock;

    public ASMFunction(String functionName) {
        this.functionName = functionName;
        this.entryBlock = new ASMBasicBlock(this, "entry");
    }

    public ASMBasicBlock getEntryBlock() {
        return entryBlock;
    }

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}

package AST;

import AST.DefineNode.ProgramDefineNode;
import Utility.Cursor;

import java.util.ArrayList;

public class ProgramNode extends ASTNode {
    private ArrayList<ProgramDefineNode> defines = new ArrayList<>();
    private ProgramDefineNode mainFunction = null;
    private boolean invalid = false;

    public ProgramNode(Cursor cursor) {
        super(cursor);
    }

    public void addDefine(ProgramDefineNode node) {
        defines.add(node);
    }

    public ArrayList<ProgramDefineNode> getDefines() {
        return defines;
    }

    public void setMainFunction(ProgramDefineNode mainFunction) {
        if (this.mainFunction == null) this.mainFunction = mainFunction;
        else invalid = true;
    }

    public ProgramDefineNode getMainFunction() {
        return mainFunction;
    }

    public boolean isInvalid() {
        return invalid || mainFunction == null;
    }

    public String getMessage() {
        if (invalid) return "multiple main function";
        else return "no main function";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST;

import AST.DefineNode.ProgramDefineNode;
import Utility.Cursor;

import java.util.ArrayList;

public class ProgramNode extends ASTNode {
    private ArrayList<ProgramDefineNode> defines = new ArrayList<>();

    public ProgramNode(Cursor cursor) {
        super(cursor);
    }

    public void addDefine(ProgramDefineNode node) {
        defines.add(node);
    }

    public  ArrayList<ProgramDefineNode> getDefines() {
        return defines;
    }

    @Override public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

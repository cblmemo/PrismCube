package AST;

import AST.DefineNode.FunctionDefineNode;
import AST.DefineNode.ProgramDefineNode;
import Utility.Cursor;

import java.util.ArrayList;

public class ProgramNode extends ASTNode {
    private final ArrayList<ProgramDefineNode> defines = new ArrayList<>();
    private boolean invalid = false;
    private boolean hasMainFunction = false;
    private String message;

    public ProgramNode(Cursor cursor) {
        super(cursor);
    }

    public void addDefine(ProgramDefineNode node) {
        defines.add(node);
        if (node instanceof FunctionDefineNode && ((FunctionDefineNode) node).isMainFunction()) {
            if (hasMainFunction) {
                invalid = true;
                message = "multiple main function";
            } else hasMainFunction = true;
        }
    }

    public ArrayList<ProgramDefineNode> getDefines() {
        return defines;
    }

    public void finishBuild() {
        if (!hasMainFunction) {
            invalid = true;
            message = "no main function";
        }
    }

    public boolean isInvalid() {
        return invalid;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

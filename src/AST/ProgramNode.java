package AST;

import AST.DefineNode.FunctionDefineNode;
import AST.DefineNode.ProgramDefineNode;
import Utility.Cursor;

import java.util.ArrayList;

public class ProgramNode extends ASTNode {
    private ArrayList<ProgramDefineNode> defines = new ArrayList<>();
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

    public boolean isInvalid() {
        return invalid || !hasMainFunction;
    }

    public String getMessage() {
        return message == null ? "no main function" : message;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

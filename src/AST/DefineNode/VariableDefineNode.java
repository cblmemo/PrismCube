package AST.DefineNode;

import AST.ASTVisitor;
import AST.TypeNode.TypeNode;
import Utility.Cursor;

import java.util.ArrayList;

public class VariableDefineNode extends ProgramDefineNode {
    private TypeNode type;
    private ArrayList<SingleVariableDefineNode> singleDefines = new ArrayList<>();

    public VariableDefineNode(TypeNode type, Cursor cursor) {
        super(cursor);
        this.type = type;
    }

    public void addStatement(SingleVariableDefineNode node) {
        singleDefines.add(node);
    }

    public TypeNode getType() {
        return type;
    }

    public ArrayList<SingleVariableDefineNode> getSingleDefines() {
        return singleDefines;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST.DefineNode;

import AST.ASTVisitor;
import AST.PrimaryNode.IdentifierPrimaryNode;
import Utility.Cursor;

import java.util.ArrayList;
import java.util.Objects;

public class ClassDefineNode extends ProgramDefineNode {
    private IdentifierPrimaryNode className;
    private ArrayList<VariableDefineNode> members = new ArrayList<>();
    private ArrayList<FunctionDefineNode> methods = new ArrayList<>();
    private ConstructorDefineNode constructor = null;
    private boolean invalid = false;
    private String message;

    public ClassDefineNode(IdentifierPrimaryNode className, Cursor cursor) {
        super(cursor);
        this.className = className;
    }

    public void addVariable(VariableDefineNode node) {
        members.add(node);
    }

    public void addFunction(FunctionDefineNode node) {
        methods.add(node);
    }

    public void setConstructor(ConstructorDefineNode node) {
        if (constructor != null) {
            invalid = true;
            message = "multiple constructors";
        } else if (!Objects.equals(node.getConstructorName(), getClassName())) {
            invalid = true;
            message = "constructor name doesn't match with class name";
        } else constructor = node;
    }

    public boolean hasCustomConstructor() {
        return constructor != null;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public String getMessage() {
        return message;
    }

    public String getClassName() {
        return className.getIdentifier();
    }

    public ArrayList<VariableDefineNode> getMembers() {
        return members;
    }

    public ArrayList<FunctionDefineNode> getMethods() {
        return methods;
    }

    public ConstructorDefineNode getConstructor() {
        return constructor;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

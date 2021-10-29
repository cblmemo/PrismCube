package AST.DefineNode;

import AST.ASTVisitor;
import AST.PrimaryNode.IdentifierPrimaryNode;
import Utility.Cursor;

import java.util.ArrayList;
import java.util.Objects;

public class ClassDefineNode extends ProgramDefineNode {
    private IdentifierPrimaryNode className;
    private ArrayList<VariableDefineNode> variables = new ArrayList<>();
    private ArrayList<FunctionDefineNode> functions = new ArrayList<>();
    private ConstructorDefineNode constructor = null;
    private boolean invalid = false;
    private String message;

    public ClassDefineNode(IdentifierPrimaryNode className, Cursor cursor) {
        super(cursor);
        this.className = className;
    }

    public void addVariable(VariableDefineNode node) {
        variables.add(node);
    }

    public void addFunction(FunctionDefineNode node) {
        functions.add(node);
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

    public boolean isInvalid() {
        return invalid;
    }

    public String getMessage() {
        return message;
    }

    public String getClassName() {
        return className.getIdentifier();
    }

    public ArrayList<VariableDefineNode> getVariables() {
        return variables;
    }

    public ArrayList<FunctionDefineNode> getFunctions() {
        return functions;
    }

    public ConstructorDefineNode getConstructor() {
        return constructor;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

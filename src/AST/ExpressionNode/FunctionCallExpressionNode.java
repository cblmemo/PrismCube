package AST.ExpressionNode;

import AST.ASTVisitor;
import Utility.Cursor;

import java.util.ArrayList;

public class FunctionCallExpressionNode extends ExpressionNode {
    private final ExpressionNode function;
    private final ArrayList<ExpressionNode> arguments = new ArrayList<>();
    private String functionName;
    private ExpressionNode instance = null;
    private boolean invalid = false;

    public FunctionCallExpressionNode(ExpressionNode function, Cursor cursor) {
        super(false, cursor);
        this.function = function;
    }

    public void addArgument(ExpressionNode node) {
        arguments.add(node);
    }

    public ExpressionNode getFunction() {
        return function;
    }

    public ArrayList<ExpressionNode> getArguments() {
        return arguments;
    }

    public ExpressionNode getArgument(int index) {
        return arguments.get(index);
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setInstance(ExpressionNode instance) {
        this.instance = instance;
    }

    public ExpressionNode getInstance() {
        return instance;
    }

    public boolean isClassMethod() {
        return getInstance() != null;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

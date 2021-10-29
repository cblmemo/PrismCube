package AST.ExpressionNode;

import AST.ASTVisitor;
import Utility.Cursor;

import java.util.ArrayList;

public class FunctionCallExpressionNode extends ExpressionNode {
    private ExpressionNode function;
    private ArrayList<ExpressionNode> arguments = new ArrayList<>();

    public FunctionCallExpressionNode(ExpressionNode function, Cursor cursor) {
        super(cursor);
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

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

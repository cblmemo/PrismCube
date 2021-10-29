package AST.ExpressionNode;

import AST.ASTVisitor;
import AST.DefineNode.ParameterDefineNode;
import AST.StatementNode.StatementNode;
import Utility.Cursor;

import java.util.ArrayList;

public class LambdaExpressionNode extends ExpressionNode {
    private ArrayList<ParameterDefineNode> parameters = null;
    private ArrayList<StatementNode> statements = new ArrayList<>();
    private ArrayList<ExpressionNode> arguments = new ArrayList<>();

    public LambdaExpressionNode(Cursor cursor) {
        super(cursor);
    }

    public void createParameterList() {
        parameters = new ArrayList<>();
    }

    public void addParameter(ParameterDefineNode node) {
        parameters.add(node);
    }

    public void addStatement(StatementNode node) {
        statements.add(node);
    }

    public void addArgument(ExpressionNode node) {
        arguments.add(node);
    }

    public ArrayList<ParameterDefineNode> getParameters() {
        return parameters;
    }

    public ArrayList<StatementNode> getStatements() {
        return statements;
    }

    public ArrayList<ExpressionNode> getArguments() {
        return arguments;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

package AST.DefineNode;

import AST.ASTVisitor;
import AST.PrimaryNode.IdentifierPrimaryNode;
import AST.StatementNode.StatementNode;
import AST.TypeNode.TypeNode;
import Utility.Cursor;

import java.util.ArrayList;
import java.util.Objects;

public class FunctionDefineNode extends ProgramDefineNode {
    private final TypeNode returnType;
    private final IdentifierPrimaryNode functionName;
    private final ArrayList<ParameterDefineNode> parameters = new ArrayList<>();
    private final ArrayList<StatementNode> statements = new ArrayList<>();

    public FunctionDefineNode(IdentifierPrimaryNode functionName, TypeNode returnType, Cursor cursor) {
        super(cursor);
        this.functionName = functionName;
        this.returnType = returnType;
    }

    public TypeNode getReturnType() {
        return returnType;
    }

    public String getFunctionName() {
        return functionName.getIdentifier();
    }

    public ArrayList<ParameterDefineNode> getParameters() {
        return parameters;
    }

    public ArrayList<StatementNode> getStatements() {
        return statements;
    }

    public void addParameter(ParameterDefineNode node) {
        parameters.add(node);
    }

    public void addStatement(StatementNode node) {
        statements.add(node);
    }

    public boolean isMainFunction() {
        return Objects.equals(getFunctionName(), "main") && Objects.equals(returnType.getTypeName(), "int") && parameters.size() == 0;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

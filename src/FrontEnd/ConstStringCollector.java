package FrontEnd;

import AST.ASTVisitor;
import AST.DefineNode.*;
import AST.ExpressionNode.*;
import AST.PrimaryNode.*;
import AST.ProgramNode;
import AST.StatementNode.*;
import AST.TypeNode.*;
import IR.IRModule;
import Memory.Memory;

import static Debug.MemoLog.log;

/**
 * This class collect all string constants
 * and store it to IR module.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class ConstStringCollector implements ASTVisitor {
    private IRModule module;

    /**
     * This method collect all const string
     * in source code and store them in IRModule.
     */
    public void collect(Memory memory) {
        if (memory.buildIR()) {
            log.Infof("Collect const string started.\n");
            module = memory.getIRModule();
            memory.getASTRoot().accept(this);
            log.Infof("Collect const string finished.\n");
        }
    }

    @Override
    public void visit(ProgramNode node) {
        node.getDefines().forEach(define -> define.accept(this));
    }

    @Override
    public void visit(ClassDefineNode node) {
        node.getMembers().forEach(member -> member.accept(this));
        node.getMethods().forEach(method -> method.accept(this));
        if (node.hasCustomConstructor()) node.getConstructor().accept(this);
    }

    @Override
    public void visit(VariableDefineNode node) {
        node.getSingleDefines().forEach(define -> define.accept(this));
    }

    @Override
    public void visit(SingleVariableDefineNode node) {
        if (node.hasInitializeValue()) node.getInitializeValue().accept(this);
    }

    @Override
    public void visit(ConstructorDefineNode node) {
        node.getStatements().forEach(statement -> statement.accept(this));
    }

    @Override
    public void visit(FunctionDefineNode node) {
        node.getStatements().forEach(statement -> statement.accept(this));
    }

    @Override
    public void visit(ParameterDefineNode node) {

    }

    @Override
    public void visit(BlockStatementNode node) {
        node.getStatements().forEach(statement -> statement.accept(this));
    }

    @Override
    public void visit(IfStatementNode node) {
        node.getConditionExpression().accept(this);
        node.getTrueStatement().accept(this);
        if (node.hasElse()) node.getFalseStatement().accept(this);
    }

    @Override
    public void visit(ForStatementNode node) {
        if (node.hasInitializeStatement()) node.getInitializeStatement().accept(this);
        if (node.hasConditionExpression()) node.getConditionExpression().accept(this);
        if (node.hasStepExpression()) node.getStepExpression().accept(this);
        node.getLoopBody().accept(this);
    }

    @Override
    public void visit(WhileStatementNode node) {
        node.getConditionExpression().accept(this);
        node.getLoopBody().accept(this);
    }

    @Override
    public void visit(ReturnStatementNode node) {
        if (node.hasReturnValue()) node.getReturnValue().accept(this);
    }

    @Override
    public void visit(BreakStatementNode node) {

    }

    @Override
    public void visit(ContinueStatementNode node) {

    }

    @Override
    public void visit(ExpressionStatementNode node) {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(EmptyStatementNode node) {

    }

    @Override
    public void visit(NewTypeExpressionNode node) {

    }

    @Override
    public void visit(MemberAccessExpressionNode node) {
        node.getInstance().accept(this);
    }

    @Override
    public void visit(LambdaExpressionNode node) {

    }

    @Override
    public void visit(FunctionCallExpressionNode node) {
        node.getFunction().accept(this);
        node.getArguments().forEach(argument -> argument.accept(this));
    }

    @Override
    public void visit(AddressingExpressionNode node) {
        node.getArray().accept(this);
        node.getIndex().accept(this);
    }

    @Override
    public void visit(PostCrementExpressionNode node) {
        node.getLhs().accept(this);
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        node.getRhs().accept(this);
    }

    @Override
    public void visit(BinaryExpressionNode node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
    }

    @Override
    public void visit(AssignExpressionNode node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
    }

    @Override
    public void visit(ThisPrimaryNode node) {

    }

    @Override
    public void visit(IdentifierPrimaryNode node) {

    }

    @Override
    public void visit(NumericalConstantPrimaryNode node) {

    }

    @Override
    public void visit(BoolConstantPrimaryNode node) {

    }

    @Override
    public void visit(StringConstantPrimaryNode node) {
        String value = node.getStringConstant();
        log.Debugf("visit a string constant %s\n", value);
        module.addNewConstString(value);
    }

    @Override
    public void visit(NullConstantPrimaryNode node) {

    }

    @Override
    public void visit(ReturnTypeNode node) {

    }

    @Override
    public void visit(SpecialTypeNode node) {

    }

    @Override
    public void visit(ArrayTypeNode node) {

    }

    @Override
    public void visit(ClassTypeNode node) {

    }

    @Override
    public void visit(BuiltinTypeNode node) {

    }
}

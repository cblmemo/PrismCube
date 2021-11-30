package FrontEnd;

import AST.ASTVisitor;
import AST.DefineNode.*;
import AST.ExpressionNode.*;
import AST.PrimaryNode.*;
import AST.ProgramNode;
import AST.StatementNode.*;
import AST.TypeNode.*;
import Memory.Memory;
import Utility.ConstExpr.ConstExprEntry;
import Utility.Entity.VariableEntity;
import Utility.Scope.*;

import java.util.Objects;

import static Debug.MemoLog.log;

/**
 * This class calculate all constexpr,
 * i.e., all expression that determined
 * by constants and functions. The trivial
 * implement of this class does not support
 * propagate of string concatenate since
 * it will produce new IRConstString.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class ConstExprCalculator implements ASTVisitor {
    private GlobalScope globalScope;
    private Scope currentScope;

    public void calculate(Memory memory) {
        if (memory.calculateConstexpr()) {
            log.Infof("Calculate expression started.\n");
            currentScope = globalScope = memory.getGlobalScope();
            memory.getASTRoot().accept(this);
            log.Infof("Calculate expression finished.\n");
        }
    }

    @Override
    public void visit(ProgramNode node) {
        node.getDefines().forEach(define -> define.accept(this));
    }

    @Override
    public void visit(ClassDefineNode node) {
        currentScope = globalScope.getClass(node.getClassName()).getClassScope();
        node.getMembers().forEach(member -> member.accept(this));
        node.getMethods().forEach(method -> method.accept(this));
        node.getConstructor().accept(this);
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(VariableDefineNode node) {
        node.getSingleDefines().forEach(define -> define.accept(this));
    }

    @Override
    public void visit(SingleVariableDefineNode node) {
        if (node.hasInitializeValue()) {
            node.getInitializeValue().accept(this);
            currentScope.getVariableEntityRecursively(node.getVariableNameStr()).setConstexprEntry(node.getInitializeValue().getEntry());
        } else if (node.getType().toType(globalScope).isNullAssignable()) {
            // array type or class type variable without initial value is null
            currentScope.getVariableEntityRecursively(node.getVariableNameStr()).setConstexprEntry(ConstExprEntry.nullConstExprEntry);
        } else currentScope.getVariableEntityRecursively(node.getVariableNameStr()).setConstexprEntry(ConstExprEntry.nonConstExprEntry);
        node.getVariableName().accept(this);
    }

    @Override
    public void visit(ConstructorDefineNode node) {
        currentScope = ((ClassScope) currentScope).getConstructor().getConstructorScope();
        node.getStatements().forEach(statement -> statement.accept(this));
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(FunctionDefineNode node) {
        currentScope = currentScope.getFunctionRecursively(node.getFunctionName()).getFunctionScope();
        node.getParameters().forEach(parameter -> {
            currentScope.getVariableEntityRecursively(parameter.getParameterName()).setConstexprEntry(ConstExprEntry.nonConstExprEntry);
        });
        node.getStatements().forEach(statement -> statement.accept(this));
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(ParameterDefineNode node) {

    }

    @Override
    public void visit(BlockStatementNode node) {
        if (node.getScopeId() != -1) {
            currentScope = currentScope.getBlockScope(node.getScopeId());
            assert currentScope instanceof BracesScope;
        }
        node.getStatements().forEach(statement -> statement.accept(this));
        if (node.getScopeId() != -1) currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(IfStatementNode node) {
        node.getConditionExpression().accept(this);
        currentScope = currentScope.getBlockScope(node.getScopeId());
        assert currentScope instanceof BranchScope;
        node.getTrueStatement().accept(this);
        currentScope = currentScope.getParentScope();
        if (node.hasElse()) {
            currentScope = currentScope.getBlockScope(node.getIfElseId());
            assert currentScope instanceof BranchScope;
            node.getFalseStatement().accept(this);
            currentScope = currentScope.getParentScope();
        }
    }

    @Override
    public void visit(ForStatementNode node) {
        if (node.hasInitializeExpression()) node.getInitializeExpression().accept(this);
        if (node.hasConditionExpression()) node.getConditionExpression().accept(this);
        if (node.hasStepExpression()) node.getStepExpression().accept(this);
        currentScope = currentScope.getBlockScope(node.getScopeId());
        assert currentScope instanceof LoopScope;
        node.getLoopBody().accept(this);
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(WhileStatementNode node) {
        node.getConditionExpression().accept(this);
        currentScope = currentScope.getBlockScope(node.getScopeId());
        assert currentScope instanceof LoopScope;
        node.getLoopBody().accept(this);
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(ReturnStatementNode node) {
        node.getReturnValue().accept(this);
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
        node.getDimensionExpressions().forEach(expression -> expression.accept(this));
        node.setEntry(ConstExprEntry.nonConstExprEntry);
    }

    @Override
    public void visit(MemberAccessExpressionNode node) {
        node.getInstance().accept(this);
        node.setEntry(ConstExprEntry.nonConstExprEntry);
    }

    @Override
    public void visit(LambdaExpressionNode node) {
        // won't exist if not in semantic check phase
        node.setEntry(ConstExprEntry.nonConstExprEntry);
    }

    @Override
    public void visit(FunctionCallExpressionNode node) {
        node.getFunction().accept(this);
        node.getArguments().forEach(argument -> argument.accept(this));
        // could add constexpr function inference
        node.setEntry(ConstExprEntry.nonConstExprEntry);
    }

    @Override
    public void visit(AddressingExpressionNode node) {
        node.getArray().accept(this);
        node.getIndex().accept(this);
        node.setEntry(ConstExprEntry.nonConstExprEntry);
    }

    @Override
    public void visit(PostCrementExpressionNode node) {
        node.getLhs().accept(this);
        ConstExprEntry lhsEntry = node.getLhs().getEntry();
        if (lhsEntry.isConstexpr()) node.setEntry(new ConstExprEntry(true, lhsEntry.getConstexprValueType(), lhsEntry.getConstexprValue()));
        else node.setEntry(ConstExprEntry.nonConstExprEntry);
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        node.getRhs().accept(this);
        ConstExprEntry rhsEntry = node.getRhs().getEntry();
        if (rhsEntry.isConstexpr()) {
            if (rhsEntry.getConstexprValueType() == ConstExprEntry.ConstExprValueType.boolType) {
                node.setEntry(new ConstExprEntry(true, rhsEntry.getConstexprValueType(), !((Boolean) rhsEntry.getConstexprValue())));
            } else {
                int newRhsValue = (Integer) rhsEntry.getConstexprValue();
                switch (node.getOp()) {
                    case "++" -> ++newRhsValue;
                    case "--" -> --newRhsValue;
                    case "-" -> newRhsValue = -newRhsValue;
                    case "~" -> newRhsValue = ~newRhsValue;
                }
                node.setEntry(new ConstExprEntry(true, rhsEntry.getConstexprValueType(), newRhsValue));
            }
        } else node.setEntry(ConstExprEntry.nonConstExprEntry);
    }

    @Override
    public void visit(BinaryExpressionNode node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
        ConstExprEntry lhsEntry = node.getLhs().getEntry(), rhsEntry = node.getRhs().getEntry();
        if (lhsEntry.isConstexpr() && rhsEntry.isConstexpr()) {
            switch (node.getOp()) {
                case "*", "/", "%", "-", "<<", ">>", "&", "^", "|" -> {
                    int lhsValue = (Integer) lhsEntry.getConstexprValue();
                    int rhsValue = (Integer) rhsEntry.getConstexprValue();
                    int thisValue = 0;
                    switch (node.getOp()) {
                        case "*" -> thisValue = lhsValue * rhsValue;
                        case "/" -> thisValue = lhsValue / rhsValue;
                        case "%" -> thisValue = lhsValue % rhsValue;
                        case "-" -> thisValue = lhsValue - rhsValue;
                        case "<<" -> thisValue = lhsValue << rhsValue;
                        case ">>" -> thisValue = lhsValue >> rhsValue;
                        case "&" -> thisValue = lhsValue & rhsValue;
                        case "^" -> thisValue = lhsValue ^ rhsValue;
                        case "|" -> thisValue = lhsValue | rhsValue;
                    }
                    node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.intType, thisValue));
                }
                case "+" -> {
                    if (node.getLhs().getExpressionType().isInt()) {
                        int lhsValue = (Integer) lhsEntry.getConstexprValue();
                        int rhsValue = (Integer) rhsEntry.getConstexprValue();
                        node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.intType, lhsValue + rhsValue));
                    } else {
                        // string cannot propagate since it will create new const string
                        node.setEntry(ConstExprEntry.nonConstExprEntry);
                    }
                }
                case "<", "<=", ">", ">=" -> {
                    boolean thisValue = false;
                    if (node.getLhs().getExpressionType().isInt()) {
                        int lhsValue = (Integer) lhsEntry.getConstexprValue();
                        int rhsValue = (Integer) rhsEntry.getConstexprValue();
                        switch (node.getOp()) {
                            case "<" -> thisValue = lhsValue < rhsValue;
                            case "<=" -> thisValue = lhsValue <= rhsValue;
                            case ">" -> thisValue = lhsValue > rhsValue;
                            case ">=" -> thisValue = lhsValue >= rhsValue;
                        }
                    } else {
                        String lhsValue = (String) lhsEntry.getConstexprValue();
                        String rhsValue = (String) rhsEntry.getConstexprValue();
                        switch (node.getOp()) {
                            case "<" -> thisValue = lhsValue.compareTo(rhsValue) < 0;
                            case "<=" -> thisValue = lhsValue.compareTo(rhsValue) <= 0;
                            case ">" -> thisValue = lhsValue.compareTo(rhsValue) > 0;
                            case ">=" -> thisValue = lhsValue.compareTo(rhsValue) >= 0;
                        }
                    }
                    node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.boolType, thisValue));
                }
                case "==" -> {
                    if (lhsEntry.isNull() || rhsEntry.isNull()) {
                        node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.boolType, lhsEntry.getConstexprValueType() == rhsEntry.getConstexprValueType()));
                    } else node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.boolType, Objects.equals(lhsEntry.getConstexprValue(), rhsEntry.getConstexprValue())));
                }
                case "!=" -> {
                    if (lhsEntry.isNull() || rhsEntry.isNull()) {
                        node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.boolType, lhsEntry.getConstexprValueType() != rhsEntry.getConstexprValueType()));
                    } else node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.boolType, !Objects.equals(lhsEntry.getConstexprValue(), rhsEntry.getConstexprValue())));
                }
                case "&&" -> node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.boolType, ((boolean) lhsEntry.getConstexprValue()) && ((boolean) rhsEntry.getConstexprValue())));
                case "||" -> node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.boolType, ((boolean) lhsEntry.getConstexprValue()) || ((boolean) rhsEntry.getConstexprValue())));
            }
        } else node.setEntry(ConstExprEntry.nonConstExprEntry);
        log.Tracef("set binary expression <%s> constexpr entry to:\n", node.getText());
        ConstExprEntry entry = node.getEntry();
        if (entry.isConstexpr()) {
            log.Tracef("is constexpr\n");
            log.Tracef("type: %s\n", entry.getConstexprValueType());
            log.Tracef("value: %s\n", entry.getConstexprValue());
        } else log.Tracef("not constexpr\n");
    }

    @Override
    public void visit(AssignExpressionNode node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
        if (node.getRhs().getEntry().isConstexpr()) {
            node.getLhs().setEntry(node.getRhs().getEntry());
            node.setEntry(node.getRhs().getEntry());
        } else {
            node.getLhs().setEntry(ConstExprEntry.nonConstExprEntry);
            if (node.getLhs() instanceof IdentifierPrimaryNode) {
                VariableEntity identifierEntity = currentScope.getVariableEntityRecursively(((IdentifierPrimaryNode) node.getLhs()).getIdentifier());
                if (identifierEntity == null) log.Errorf("cannot find variable entity of identifier %s.\n", ((IdentifierPrimaryNode) node.getLhs()).getIdentifier());
                else identifierEntity.setConstexprEntry(ConstExprEntry.nonConstExprEntry);
                log.Debugf("identifier <%s> assigned to a non-constexpr value\n", ((IdentifierPrimaryNode) node.getLhs()).getIdentifier());
            }
            node.setEntry(ConstExprEntry.nonConstExprEntry);
        }
    }

    @Override
    public void visit(ThisPrimaryNode node) {
        node.setEntry(ConstExprEntry.nonConstExprEntry);
    }

    @Override
    public void visit(IdentifierPrimaryNode node) {
        if (node.isFunction()) node.setEntry(ConstExprEntry.nonConstExprEntry);
        else {
            VariableEntity identifierEntity = currentScope.getVariableEntityRecursively(node.getIdentifier());
            if (identifierEntity == null) log.Errorf("cannot find variable entity of identifier %s.\n", node.getIdentifier());
            else {
                node.setEntry(identifierEntity.getConstexprEntry());
                if (identifierEntity.notPrint()) {
                    identifierEntity.markAsPrinted();
                    log.Debugf("set identifier <%s> constexpr entry to:\n", node.getIdentifier());
                    ConstExprEntry entry = node.getEntry();
                    if (entry.isConstexpr()) {
                        log.Debugf("is constexpr\n");
                        log.Debugf("type: %s\n", entry.getConstexprValueType());
                        log.Debugf("value: %s\n", entry.getConstexprValue());
                    } else log.Debugf("not constexpr\n");
                }
            }
        }
    }

    @Override
    public void visit(NumericalConstantPrimaryNode node) {
        node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.intType, node.getNumericalConstant()));
    }

    @Override
    public void visit(BoolConstantPrimaryNode node) {
        node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.boolType, node.getBoolConstant()));
    }

    @Override
    public void visit(StringConstantPrimaryNode node) {
        String value = node.getStringConstant();
        node.setEntry(new ConstExprEntry(true, ConstExprEntry.ConstExprValueType.stringType, value));
    }

    @Override
    public void visit(NullConstantPrimaryNode node) {
        node.setEntry(ConstExprEntry.nullConstExprEntry);
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

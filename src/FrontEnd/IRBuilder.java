package FrontEnd;

import AST.ASTNode;
import AST.ASTVisitor;
import AST.DefineNode.*;
import AST.ExpressionNode.*;
import AST.PrimaryNode.*;
import AST.ProgramNode;
import AST.StatementNode.*;
import AST.TypeNode.*;
import IR.IRBasicBlock;
import IR.IRFunction;
import IR.IRGlobalDefine;
import IR.IRModule;
import IR.Instruction.*;
import IR.Operand.*;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;
import Memory.Memory;
import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.Scope.*;
import Utility.error.IRError;

import java.util.Objects;
import java.util.Stack;

/**
 * This class build IRModule for source code.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class IRBuilder implements ASTVisitor {
    private GlobalScope globalScope;
    private Scope currentScope;
    private IRModule module;
    private IRFunction currentFunction;
    // [[--NOTICE--]] current basic block need to be appended when overlapped
    private IRBasicBlock currentBasicBlock;

    private int labelCnt = 0;

    private enum status {
        idle, // no special status, just to make sure Stack.peak() won't RE
        allocaParameter, // alloca registers to store function parameter
        storeParameter, // store parameters' value in register
    }

    // todo remove this stack?
    private final Stack<status> currentStatus = new Stack<>();

    public void build(Memory memory) {
        if (memory.buildIR()) {
            currentScope = globalScope = memory.getGlobalScope();
            module = memory.getIRModule();
            module.initializeBuiltinFunction(globalScope);
            currentStatus.push(status.idle);
            memory.getASTRoot().accept(this);
            currentStatus.pop();
        }
    }

    @Override
    public void visit(ProgramNode node) {
        // todo collect class
        //  what if function in class?? maybe considering use Stack<status>
        //  and store IRFunction to FunctionEntity in visit FunctionDefineNode

        // collect function
        node.getDefines().forEach(define -> {
            if (define instanceof FunctionDefineNode) {
                IRFunction function = new IRFunction(((FunctionDefineNode) define).getFunctionName());
                function.setReturnType(((FunctionDefineNode) define).getReturnType().toIRType(module));
                ((FunctionDefineNode) define).getParameters().forEach(parameter -> function.addParameterType(parameter.getType().toType(globalScope), parameter.getType().toIRType(module)));
                module.addFunction(function);
                currentScope.getFunctionRecursively(((FunctionDefineNode) define).getFunctionName()).setIRFunction(function);
            }
        });

        // collect global variable
        node.getDefines().forEach(define -> {
            if (define instanceof VariableDefineNode) define.accept(this);
        });
        IRFunction globalConstructor = module.getGlobalConstructor();
        IRBasicBlock entryBlock = globalConstructor.getEntryBlock();
        module.getSingleInitializeFunctions().forEach(initFunc -> entryBlock.appendInstruction(new IRCallInstruction(module.getIRType("void"), initFunc)));
        entryBlock.setEscapeInstruction(new IRBrInstruction(null, globalConstructor.getReturnBlock(), null, entryBlock));
        entryBlock.finishBlock();
        globalConstructor.getReturnBlock().setEscapeInstruction(new IRReturnInstruction(module.getIRType("void"), null));
        globalConstructor.finishFunction();

        // todo step into classes
//        node.getDefines().forEach(define -> {
//            if (define instanceof ClassDefineNode) define.accept(this);
//        });

        // step into functions
        node.getDefines().forEach(define -> {
            if (define instanceof FunctionDefineNode) define.accept(this);
        });
    }

    @Override
    public void visit(ClassDefineNode node) {
        currentScope = globalScope.getClass(node.getClassName()).getClassScope();

        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(VariableDefineNode node) {
        if (currentScope.hasEncounteredFlow()) return;
        node.getSingleDefines().forEach(define -> define.accept(this));
    }

    @Override
    public void visit(SingleVariableDefineNode node) {
        IRTypeSystem variableIRType = node.getType().toIRType(module);
        if (currentScope instanceof GlobalScope) {
            assert currentFunction == null && currentBasicBlock == null;
            IRGlobalVariableRegister variableRegister = new IRGlobalVariableRegister(new IRPointerType(variableIRType), node.getVariableNameStr());
            IRGlobalDefine define = new IRGlobalDefine(node.getVariableNameStr(), node.getType().toIRType(module));
            currentScope.getVariableEntityRecursively(node.getVariableNameStr()).setCurrentRegister(variableRegister);
            if (node.hasInitializeValue()) {
                if (node.getInitializeValue().getEntry().isConstexpr()) define.setInitValue(currentScope.getVariableEntityRecursively(node.getVariableNameStr()).getConstexprEntry().toIROperand(module));
                else {
                    // generate an initialize function
                    IRFunction singleInitializeFunction = module.generateSingleInitializeFunction();
                    int cnt = IRRegister.getCurrentCnt();
                    currentFunction = singleInitializeFunction;
                    currentBasicBlock = singleInitializeFunction.getEntryBlock();
                    IRRegister.reset();
                    node.getInitializeValue().accept(this);
                    // similar to ReturnStatementNode
                    IROperand returnValue = node.getInitializeValue().getIRResultValue();
                    currentBasicBlock.appendInstruction(new IRStoreInstruction(variableIRType, new IRGlobalVariableRegister(variableIRType, node.getVariableNameStr()), returnValue));
                    currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, currentFunction.getReturnBlock(), null, currentBasicBlock));
                    currentFunction.getReturnBlock().appendInstruction(new IRReturnInstruction(module.getIRType("void"), null));
                    currentBasicBlock.finishBlock();
                    currentFunction.finishFunction();
                    IRRegister.resetTo(cnt);
                    currentFunction = null;
                    currentBasicBlock = null;
                }
            }
            module.addGlobalDefine(define);
        } else {
            IRRegister variableRegister = new IRRegister(new IRPointerType(variableIRType));
            currentBasicBlock.appendInstruction(new IRAllocaInstruction(variableIRType, variableRegister));
            currentScope.getVariableEntityRecursively(node.getVariableNameStr()).setCurrentRegister(variableRegister);
            if (node.hasInitializeValue()) {
                IROperand initVal;
                if (node.getInitializeValue().getEntry().isConstexpr()) initVal = node.getInitializeValue().getEntry().toIROperand(module);
                else {
                    node.getInitializeValue().accept(this);
                    initVal = node.getInitializeValue().getIRResultValue();
                }
                currentBasicBlock.appendInstruction(new IRStoreInstruction(variableIRType, variableRegister, initVal));
            } else currentBasicBlock.appendInstruction(new IRStoreInstruction(variableIRType, variableRegister, variableIRType.getDefaultValue()));
        }
    }

    @Override
    public void visit(ConstructorDefineNode node) {
        currentScope = ((ClassScope) currentScope).getConstructor().getConstructorScope();

        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(FunctionDefineNode node) {
        IRRegister.resetTo(node.getParameters().size());
        currentFunction = module.getFunction(node.getFunctionName());
        currentBasicBlock = currentFunction.getEntryBlock();
        currentScope = currentScope.getFunctionRecursively(node.getFunctionName()).getFunctionScope();
        currentStatus.push(status.allocaParameter);
        node.getParameters().forEach(parameter -> parameter.accept(this));
        currentStatus.pop();
        currentStatus.push(status.storeParameter);
        node.getParameters().forEach(parameter -> parameter.accept(this));
        currentStatus.pop();
        IRTypeSystem returnIRType = node.getReturnType().toIRType(module);
        IRRegister returnValuePtr = new IRRegister(new IRPointerType(returnIRType));
        currentBasicBlock.appendInstruction(new IRAllocaInstruction(returnIRType, returnValuePtr));
        ((FunctionScope) currentScope).setReturnValuePtr(returnValuePtr);
        node.getStatements().forEach(statement -> {
            statement.accept(this);
        });
        // void function or main could have no return statement
        if (!((FunctionScope) currentScope).hasReturnStatement()) {
            currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, currentFunction.getReturnBlock(), null, currentBasicBlock));
            currentFunction.getReturnBlock().setEscapeInstruction(new IRReturnInstruction(returnIRType, returnIRType.getDefaultValue()));
        } else {
            IRRegister returnValue = new IRRegister(returnIRType);
            currentFunction.getReturnBlock().appendInstruction(new IRLoadInstruction(returnIRType, returnValue, returnValuePtr));
            currentFunction.getReturnBlock().setEscapeInstruction(new IRReturnInstruction(returnIRType, returnValue));
        }
        currentScope = currentScope.getParentScope();
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentFunction.finishFunction();
        currentBasicBlock = null;
        currentFunction = null;
    }

    @Override
    public void visit(ParameterDefineNode node) {
        assert currentScope instanceof FunctionScope;
        if (currentStatus.peek() == status.allocaParameter) {
            IRTypeSystem parameterType = node.getType().toIRType(module);
            IRRegister parameterRegister = new IRRegister(new IRPointerType(parameterType));
            VariableEntity parameterEntity = currentScope.getVariableEntityRecursively(node.getParameterName());
            parameterEntity.setCurrentRegister(parameterRegister);
            currentBasicBlock.appendInstruction(new IRAllocaInstruction(parameterType, parameterRegister));
        }
        if (currentStatus.peek() == status.storeParameter) {
            IRTypeSystem parameterType = node.getType().toIRType(module);
            VariableEntity parameterEntity = currentScope.getVariableEntityRecursively(node.getParameterName());
            IRRegister targetRegister = parameterEntity.getCurrentRegister();
            int srcRegisterId = targetRegister.getId() - ((FunctionScope) currentScope).getParameterNumber();
            currentBasicBlock.appendInstruction(new IRStoreInstruction(parameterType, targetRegister, new IRRegister(parameterType, srcRegisterId)));
        }
    }

    @Override
    public void visit(BlockStatementNode node) {
        if (currentScope.hasEncounteredFlow()) return;
        if (node.getScopeId() != -1) currentScope = currentScope.getBlockScope(node.getScopeId());
        node.getStatements().forEach(statement -> statement.accept(this));
        if (node.getScopeId() != -1) currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(IfStatementNode node) {
        if (currentScope.hasEncounteredFlow()) return;
        // current block has already added to function
        node.getConditionExpression().accept(this);
        int id = labelCnt++;
        IRBasicBlock thenBlock = new IRBasicBlock(currentFunction, id + "_if_then");
        IRBasicBlock elseBlock = new IRBasicBlock(currentFunction, id + "_if_else");
        IRBasicBlock terminateBlock = new IRBasicBlock(currentFunction, id + "_if_terminate");
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(node.getConditionExpression().getIRResultValue(), thenBlock, node.hasElse() ? elseBlock : terminateBlock, currentBasicBlock));
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentBasicBlock = thenBlock;
        currentScope = currentScope.getBlockScope(node.getScopeId());
        node.getTrueStatement().accept(this);
        currentScope = currentScope.getParentScope();
        // everytime encountered a statement, need to check whether it has escape or not
        if (!currentBasicBlock.hasEscapeInstruction()) // return statement might generate an escape instruction
            currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, terminateBlock, null, currentBasicBlock));
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        if (node.hasElse()) {
            currentBasicBlock = elseBlock;
            currentScope = currentScope.getBlockScope(node.getIfElseId());
            node.getFalseStatement().accept(this);
            currentScope = currentScope.getParentScope();
            if (!currentBasicBlock.hasEscapeInstruction()) // return statement might always generate an escape instruction
                currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, terminateBlock, null, currentBasicBlock));
            currentBasicBlock.finishBlock();
            currentFunction.appendBasicBlock(currentBasicBlock);
        }
        currentBasicBlock = terminateBlock;
    }

    @Override
    public void visit(ForStatementNode node) {
        if (currentScope.hasEncounteredFlow()) return;
        currentScope = currentScope.getBlockScope(node.getScopeId());
        int id = labelCnt++;
        IRBasicBlock conditionBlock = new IRBasicBlock(currentFunction, id + "_for_condition");
        IRBasicBlock bodyBlock = new IRBasicBlock(currentFunction, id + "_for_body");
        IRBasicBlock terminateBlock = new IRBasicBlock(currentFunction, id + "_for_terminate");
        if (node.hasInitializeStatement()) node.getInitializeStatement().accept(this);
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, conditionBlock, null, currentBasicBlock));
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentBasicBlock = conditionBlock;
        IROperand conditionResult;
        if (node.hasConditionExpression()) {
            node.getConditionExpression().accept(this);
            conditionResult = node.getConditionExpression().getIRResultValue();
        } else conditionResult = new IRConstBool(module.getIRType("bool"), true);
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(conditionResult, bodyBlock, terminateBlock, currentBasicBlock));
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentBasicBlock = bodyBlock;
        LoopScope loopScope = currentScope.getLoopScope();
        loopScope.setLoopConditionBlock(conditionBlock);
        loopScope.setLoopTerminateBlock(terminateBlock);
        node.getLoopBody().accept(this);
        // might encounter return, break or continue
        if (!currentBasicBlock.hasEscapeInstruction()) {
            if (node.hasStepExpression()) node.getStepExpression().accept(this);
            currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, conditionBlock, null, currentBasicBlock));
        }
        currentScope = currentScope.getParentScope();
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentBasicBlock = terminateBlock;
    }

    @Override
    public void visit(WhileStatementNode node) {
        if (currentScope.hasEncounteredFlow()) return;
        int id = labelCnt++;
        IRBasicBlock conditionBlock = new IRBasicBlock(currentFunction, id + "_while_condition");
        IRBasicBlock bodyBlock = new IRBasicBlock(currentFunction, id + "_while_body");
        IRBasicBlock terminateBlock = new IRBasicBlock(currentFunction, id + "_while_terminate");
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, conditionBlock, null, currentBasicBlock));
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentBasicBlock = conditionBlock;
        node.getConditionExpression().accept(this);
        IROperand conditionResult = node.getConditionExpression().getIRResultValue();
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(conditionResult, bodyBlock, terminateBlock, currentBasicBlock));
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentBasicBlock = bodyBlock;
        currentScope = currentScope.getBlockScope(node.getScopeId());
        LoopScope loopScope = currentScope.getLoopScope();
        loopScope.setLoopConditionBlock(conditionBlock);
        loopScope.setLoopTerminateBlock(terminateBlock);
        node.getLoopBody().accept(this);
        currentScope = currentScope.getParentScope();
        // might encounter return, break or continue
        if (!currentBasicBlock.hasEscapeInstruction())
            currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, conditionBlock, null, currentBasicBlock));
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentBasicBlock = terminateBlock;
    }

    @Override
    public void visit(ReturnStatementNode node) {
        if (currentScope.hasEncounteredFlow()) return;
        node.getReturnValue().accept(this);
        IROperand returnValueRegister = node.getReturnValue().getIRResultValue();
        // return value should be store in a specific IRRegister, which will be created at the
        // entry block, i.e., the first time visit FunctionDefineNode, and escape block return that register.
        currentBasicBlock.appendInstruction(new IRStoreInstruction(currentFunction.getReturnType(), currentScope.getReturnValuePtr(), returnValueRegister));
        // return statement should create an escape (i.e., branch) instruction to returnBlock.
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, currentFunction.getReturnBlock(), null, currentBasicBlock));
        currentScope.setAsEncounteredFlow();
    }

    @Override
    public void visit(BreakStatementNode node) {
        LoopScope loopScope = currentScope.getLoopScope();
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, loopScope.getLoopTerminateBlock(), null, currentBasicBlock));
        currentScope.setAsEncounteredFlow();
    }

    @Override
    public void visit(ContinueStatementNode node) {
        LoopScope loopScope = currentScope.getLoopScope();
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, loopScope.getLoopConditionBlock(), null, currentBasicBlock));
        currentScope.setAsEncounteredFlow();
    }

    @Override
    public void visit(ExpressionStatementNode node) {
        if (currentScope.hasEncounteredFlow()) return;
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

    }

    @Override
    public void visit(LambdaExpressionNode node) {

    }

    @Override
    public void visit(FunctionCallExpressionNode node) {
        if (node.isClassMethod()) {
            // todo call class method
        } else {
            // the most critical code to print Hello, Happy World!
            FunctionEntity entity = currentScope.getFunctionRecursively(node.getFunctionName());
            IRFunction function = entity.getIRFunction();
            IRCallInstruction inst = new IRCallInstruction(node.getExpressionType().toIRType(module), function);
            node.getArguments().forEach(argument -> {
                if (argument.getEntry().isConstexpr()) inst.addArgument(argument.getEntry().toIROperand(module), argument.getExpressionType().toIRType(module));
                else {
                    argument.accept(this);
                    inst.addArgument(argument.getIRResultValue(), argument.getExpressionType().toIRType(module));
                }
            });
            if (!entity.getReturnType().isVoid()) {
                IRRegister resultRegister = new IRRegister(entity.getReturnType().toIRType(module));
                inst.setResultRegister(resultRegister);
                node.setIRResultValue(resultRegister);
            }
            currentBasicBlock.appendInstruction(inst);
        }
    }

    @Override
    public void visit(AddressingExpressionNode node) {

    }

    @Override
    public void visit(PostCrementExpressionNode node) {
        if (node.getEntry().isConstexpr()) {
            node.setIRResultValue(node.getEntry().toIROperand(module));
            return;
        }
        IROperand lhsVal;
        if (node.getLhs().getEntry().isConstexpr()) lhsVal = node.getLhs().getEntry().toIROperand(module);
        else {
            node.getLhs().accept(this);
            lhsVal = node.getLhs().getIRResultValue();
        }
        IRTypeSystem resultType = node.getExpressionType().toIRType(module);
        assert resultType.isInt();
        IRRegister tempRegister = new IRRegister(resultType);
        IRRegister resultRegister = new IRRegister(resultType);
        ASTNode bottomLeftValueNode = node.getLhs().getBottomLeftValueNode();
        if (bottomLeftValueNode instanceof IdentifierPrimaryNode) {
            VariableEntity entity = currentScope.getVariableEntityRecursively(((IdentifierPrimaryNode) bottomLeftValueNode).getIdentifier());
            IRRegister currentRegister = entity.getCurrentRegister();
            currentBasicBlock.appendInstruction(new IRLoadInstruction(resultType, tempRegister, currentRegister));
            IRTypeSystem variableType = ((IdentifierPrimaryNode) bottomLeftValueNode).getExpressionType().toIRType(module);
            currentBasicBlock.appendInstruction(new IRBinaryInstruction("add", resultRegister, new IRConstInt(module.getIRType("int"), Objects.equals(node.getOp(), "++") ? 1 : -1), lhsVal));
            currentBasicBlock.appendInstruction(new IRStoreInstruction(variableType, currentRegister, resultRegister));
            node.setIRResultValue(tempRegister);
        } else if (bottomLeftValueNode instanceof AddressingExpressionNode) {
            // todo
        } else {
            assert bottomLeftValueNode instanceof MemberAccessExpressionNode;

        }
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        if (node.getEntry().isConstexpr()) {
            node.setIRResultValue(node.getEntry().toIROperand(module));
            return;
        }
        IROperand rhsVal;
        if (node.getRhs().getEntry().isConstexpr()) rhsVal = node.getRhs().getEntry().toIROperand(module);
        else {
            node.getRhs().accept(this);
            rhsVal = node.getRhs().getIRResultValue();
        }
        IRTypeSystem resultType = node.getExpressionType().toIRType(module);
        IRRegister resultRegister = new IRRegister(resultType);
        if (resultType.isBool()) {
            assert Objects.equals(node.getOp(), "!");
            IRRegister rhsCharVal = new IRRegister(module.getIRType("char"));
            currentBasicBlock.appendInstruction(new IRZextInstruction(rhsCharVal, rhsVal, module.getIRType("char")));
            IRRegister resultCharVal = new IRRegister(module.getIRType("char"));
            currentBasicBlock.appendInstruction(new IRBinaryInstruction("xor", resultCharVal, new IRConstChar(module.getIRType("int"), 1), rhsCharVal));
            currentBasicBlock.appendInstruction(new IRTruncInstruction(resultRegister, resultCharVal, module.getIRType("bool")));
        } else {
            assert resultType.isInt();
            if (Objects.equals(node.getOp(), "++") || Objects.equals(node.getOp(), "--")) {
                ASTNode bottomLeftValueNode = node.getRhs().getBottomLeftValueNode();
                if (bottomLeftValueNode instanceof IdentifierPrimaryNode) {
                    VariableEntity entity = currentScope.getVariableEntityRecursively(((IdentifierPrimaryNode) bottomLeftValueNode).getIdentifier());
                    IRRegister currentRegister = entity.getCurrentRegister();
                    IRTypeSystem variableType = ((IdentifierPrimaryNode) bottomLeftValueNode).getExpressionType().toIRType(module);
                    currentBasicBlock.appendInstruction(new IRBinaryInstruction("add", resultRegister, new IRConstInt(module.getIRType("int"), Objects.equals(node.getOp(), "++") ? 1 : -1), rhsVal));
                    currentBasicBlock.appendInstruction(new IRStoreInstruction(variableType, currentRegister, resultRegister));
                    node.setIRResultValue(resultRegister);
                } else if (bottomLeftValueNode instanceof AddressingExpressionNode) {
                    // todo
                } else {
                    assert bottomLeftValueNode instanceof MemberAccessExpressionNode;

                }
            } else {
                String op;
                int lhsVal;
                switch (node.getOp()) {
                    case "+" -> {
                        op = "add";
                        lhsVal = 0;
                    }
                    case "-" -> {
                        op = "sub nsw";
                        lhsVal = 0;
                    }
                    case "~" -> {
                        op = "xor";
                        lhsVal = ~-1;
                    }
                    default -> throw new IRError("invalid unary op");
                }
                currentBasicBlock.appendInstruction(new IRBinaryInstruction(op, resultRegister, new IRConstInt(module.getIRType("int"), lhsVal), rhsVal));
            }
        }
        node.setIRResultValue(resultRegister);
    }

    @Override
    public void visit(BinaryExpressionNode node) {
        if (node.getEntry().isConstexpr()) {
            node.setIRResultValue(node.getEntry().toIROperand(module));
            return;
        }
        IROperand lhsVal, rhsVal;
        if (node.getLhs().getEntry().isConstexpr()) lhsVal = node.getLhs().getEntry().toIROperand(module);
        else {
            node.getLhs().accept(this);
            lhsVal = node.getLhs().getIRResultValue();
        }
        if (node.getRhs().getEntry().isConstexpr()) rhsVal = node.getRhs().getEntry().toIROperand(module);
        else {
            node.getRhs().accept(this);
            rhsVal = node.getRhs().getIRResultValue();
        }
        IRTypeSystem resultType = node.getExpressionType().toIRType(module);
        if (resultType.isInt()) {
            IRRegister resultRegister = new IRRegister(resultType);
            String op;
            switch (node.getOp()) {
                case "+" -> op = "add";
                case "-" -> op = "sub nsw"; // nsw stands for no signed wrap, which will set result to poison value if encounter overflow
                case "*" -> op = "mul";
                case "/" -> op = "sdiv"; // signed division
                case "%" -> op = "srem"; // signed remainder
                case "<<" -> op = "shl nsw";
                case ">>" -> op = "ashr nsw";
                case "&" -> op = "and";
                case "^" -> op = "xor";
                case "|" -> op = "or";
                default -> throw new IRError("invalid binary op");
            }
            currentBasicBlock.appendInstruction(new IRBinaryInstruction(op, resultRegister, lhsVal, rhsVal));
            node.setIRResultValue(resultRegister);
        } else if (resultType.isBool()) {
            if (Objects.equals(node.getOp(), "&&") || Objects.equals(node.getOp(), "||")) { // bool logic arithmetic
                IRRegister lhsCharVal = new IRRegister(module.getIRType("char"));
                currentBasicBlock.appendInstruction(new IRZextInstruction(lhsCharVal, lhsVal, module.getIRType("char")));
                IRRegister rhsCharVal = new IRRegister(module.getIRType("char"));
                currentBasicBlock.appendInstruction(new IRZextInstruction(rhsCharVal, rhsVal, module.getIRType("char")));
                IRRegister resultCharVal = new IRRegister(module.getIRType("char"));
                IRRegister resultRegister = new IRRegister(resultType);
                currentBasicBlock.appendInstruction(new IRBinaryInstruction(Objects.equals(node.getOp(), "&&") ? "and" : "or", resultCharVal, lhsCharVal, rhsCharVal));
                currentBasicBlock.appendInstruction(new IRTruncInstruction(resultRegister, resultCharVal, module.getIRType("bool")));
                node.setIRResultValue(resultRegister);
            } else { // cmp
                if (node.getLhs().getExpressionType().isInt()) {
                    IRRegister resultRegister = new IRRegister(resultType);
                    String op;
                    switch (node.getOp()) { // "s" stands for signed
                        case "<" -> op = "slt";
                        case "<=" -> op = "sle";
                        case ">" -> op = "sgt";
                        case ">=" -> op = "sge";
                        case "==" -> op = "eq";
                        case "!=" -> op = "ne";
                        default -> throw new IRError("invalid binary op");
                    }
                    currentBasicBlock.appendInstruction(new IRIcmpInstruction(op, resultRegister, lhsVal, rhsVal));
                    node.setIRResultValue(resultRegister);
                } else if (node.getLhs().getExpressionType().isString()) {
                    FunctionEntity entity;
                    switch (node.getOp()) {
                        case "<" -> entity = currentScope.getFunctionRecursively("__mx_stringLt");
                        case "<=" -> entity = currentScope.getFunctionRecursively("__mx_stringLe");
                        case ">" -> entity = currentScope.getFunctionRecursively("__mx_stringGt");
                        case ">=" -> entity = currentScope.getFunctionRecursively("__mx_stringGe");
                        case "==" -> entity = currentScope.getFunctionRecursively("__mx_stringEq");
                        case "!=" -> entity = currentScope.getFunctionRecursively("__mx_stringNe");
                        default -> throw new IRError("invalid binary op");
                    }
                    IRFunction cmpFunction = entity.getIRFunction();
                    IRRegister boolTempRegister = new IRRegister(module.getIRType("char"));
                    IRCallInstruction inst = new IRCallInstruction(module.getIRType("char"), cmpFunction);
                    inst.addArgument(lhsVal, module.getIRType("string")).addArgument(rhsVal, module.getIRType("string"));
                    inst.setResultRegister(boolTempRegister);
                    currentBasicBlock.appendInstruction(inst);
                    // trunc char return value to i1
                    IRRegister resultRegister = new IRRegister(resultType);
                    currentBasicBlock.appendInstruction(new IRTruncInstruction(resultRegister, boolTempRegister, module.getIRType("bool")));
                    node.setIRResultValue(resultRegister);
                } else {
                    // todo support class == null && array != null

                }
            }
        } else { // resultType.isString
            assert resultType.isString();
            // manually call strcat in c to implement string +
            assert Objects.equals(node.getOp(), "+");
            IRRegister resultRegister = new IRRegister(resultType);
            FunctionEntity entity = currentScope.getFunctionRecursively("__mx_concatenateString");
            IRFunction function = entity.getIRFunction();
            IRCallInstruction inst = new IRCallInstruction(module.getIRType("string"), function);
            inst.addArgument(lhsVal, module.getIRType("string")).addArgument(rhsVal, module.getIRType("string"));
            inst.setResultRegister(resultRegister);
            currentBasicBlock.appendInstruction(inst);
            node.setIRResultValue(resultRegister);
        }
    }

    @Override
    public void visit(AssignExpressionNode node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);
        // left value: variable, array addressing, assign, member access, prefix crement
        // self-crement need update register of original left value since value has changed
        ASTNode bottomLeftValueNode = node.getBottomLeftValueNode();
        if (bottomLeftValueNode instanceof IdentifierPrimaryNode) {
            VariableEntity entity = currentScope.getVariableEntityRecursively(((IdentifierPrimaryNode) bottomLeftValueNode).getIdentifier());
            IRRegister currentRegister = entity.getCurrentRegister();
            IRTypeSystem variableType = ((IdentifierPrimaryNode) bottomLeftValueNode).getExpressionType().toIRType(module);
            currentBasicBlock.appendInstruction(new IRStoreInstruction(variableType, currentRegister, node.getRhs().getIRResultValue()));
            node.setIRResultValue(node.getRhs().getIRResultValue());
        } else if (bottomLeftValueNode instanceof AddressingExpressionNode) {
            // todo
        } else {
            assert bottomLeftValueNode instanceof MemberAccessExpressionNode;

        }
    }

    @Override
    public void visit(ThisPrimaryNode node) {

    }

    @Override
    public void visit(IdentifierPrimaryNode node) {
        if (!node.isVariable()) throw new IRError("visit FunctionIdentifier in IRBuilder");
        VariableEntity entity = currentScope.getVariableEntityRecursively(node.getIdentifier());
        IRTypeSystem loadType = entity.getVariableType().toIRType(module);
        IRRegister loadTarget = new IRRegister(loadType);
        // global variables always use @identifier to store value, therefore
        // local variable value is stored in registers, and might change when updated
        // so load target need update to VariableEntity
        // access to local variable need load first, otherwise will cause type error
        currentBasicBlock.appendInstruction(new IRLoadInstruction(loadType, loadTarget, entity.getCurrentRegister()));
        node.setIRResultValue(loadTarget);
    }

    @Override
    public void visit(NumericalConstantPrimaryNode node) {
        // avoid using constexpr entry to support running without ConstExprCalculator
        node.setIRResultValue(new IRConstInt(module.getIRType("int"), node.getNumericalConstant()));
    }

    @Override
    public void visit(BoolConstantPrimaryNode node) {
        node.setIRResultValue(new IRConstBool(module.getIRType("bool"), node.getBoolConstant()));
    }

    @Override
    public void visit(StringConstantPrimaryNode node) {
        node.setIRResultValue(new IRConstString(module.getIRType("string"), node.getStringConstant(), module.getConstStringId(node.getStringConstant())));
    }

    @Override
    public void visit(NullConstantPrimaryNode node) {
        node.setIRResultValue(new IRNull(module.getIRType("void")));
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

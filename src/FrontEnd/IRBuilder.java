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

import java.util.LinkedList;
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

    private IRTypeSystem getBoolType() {
        return module.getIRType("bool");
    }

    private IRTypeSystem getCharType() {
        return module.getIRType("char");
    }

    private IRTypeSystem getIntType() {
        return module.getIRType("int");
    }

    private IRTypeSystem getStringType() {
        return module.getIRType("string");
    }

    private IRTypeSystem getVoidType() {
        return module.getIRType("void");
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
        module.getSingleInitializeFunctions().forEach(initFunc -> entryBlock.appendInstruction(new IRCallInstruction(getVoidType(), initFunc)));
        entryBlock.setEscapeInstruction(new IRBrInstruction(null, globalConstructor.getReturnBlock(), null, entryBlock));
        entryBlock.finishBlock();
        globalConstructor.getReturnBlock().setEscapeInstruction(new IRReturnInstruction(getVoidType(), null));
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
                    currentBasicBlock.appendInstruction(new IRStoreInstruction(variableIRType, variableRegister, returnValue));
                    currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, currentFunction.getReturnBlock(), null, currentBasicBlock));
                    currentFunction.getReturnBlock().setEscapeInstruction(new IRReturnInstruction(getVoidType(), null));
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
            IROperand initVal;
            if (node.hasInitializeValue()) {
                if (node.getInitializeValue().getEntry().isConstexpr()) initVal = node.getInitializeValue().getEntry().toIROperand(module);
                else {
                    node.getInitializeValue().accept(this);
                    initVal = node.getInitializeValue().getIRResultValue();
                }
            } else initVal = variableIRType.getDefaultValue();
            currentBasicBlock.appendInstruction(new IRStoreInstruction(variableIRType, variableRegister, initVal));
        }
    }

    @Override
    public void visit(ConstructorDefineNode node) {
        currentScope = ((ClassScope) currentScope).getConstructor().getConstructorScope();

        // don't inherit any encountered flow mark since method has ended
        currentScope = currentScope.getParentScope();
    }

    @Override
    public void visit(FunctionDefineNode node) {
        IRRegister.resetTo(node.getParameters().size());
        currentFunction = module.getFunction(node.getFunctionName());
        currentBasicBlock = currentFunction.getEntryBlock();
        currentScope = currentScope.getFunctionRecursively(node.getFunctionName()).getFunctionScope();
        node.getParameters().forEach(parameter -> {
            IRTypeSystem parameterType = parameter.getType().toIRType(module);
            IRRegister parameterRegister = new IRRegister(new IRPointerType(parameterType));
            VariableEntity parameterEntity = currentScope.getVariableEntityRecursively(parameter.getParameterName());
            parameterEntity.setCurrentRegister(parameterRegister);
            currentBasicBlock.appendInstruction(new IRAllocaInstruction(parameterType, parameterRegister));
        });
        node.getParameters().forEach(parameter -> {
            IRTypeSystem parameterType = parameter.getType().toIRType(module);
            VariableEntity parameterEntity = currentScope.getVariableEntityRecursively(parameter.getParameterName());
            IRRegister targetRegister = parameterEntity.getCurrentRegister();
            int srcRegisterId = targetRegister.getId() - ((FunctionScope) currentScope).getParameterNumber();
            currentBasicBlock.appendInstruction(new IRStoreInstruction(parameterType, targetRegister, new IRRegister(parameterType, srcRegisterId)));
        });
        IRTypeSystem returnIRType = node.getReturnType().toIRType(module);
        if (!returnIRType.isVoid()) {
            IRRegister returnValuePtr = new IRRegister(new IRPointerType(returnIRType));
            currentBasicBlock.appendInstruction(new IRAllocaInstruction(returnIRType, returnValuePtr));
            ((FunctionScope) currentScope).setReturnValuePtr(returnValuePtr);
            node.getStatements().forEach(statement -> statement.accept(this));
            // main function could have no return statement
            if (((FunctionScope) currentScope).hasReturnStatement()) {
                IRRegister returnValue = new IRRegister(returnIRType);
                currentFunction.getReturnBlock().appendInstruction(new IRLoadInstruction(returnIRType, returnValue, returnValuePtr));
                currentFunction.getReturnBlock().setEscapeInstruction(new IRReturnInstruction(returnIRType, returnValue));
            } else currentFunction.getReturnBlock().setEscapeInstruction(new IRReturnInstruction(returnIRType, returnIRType.getDefaultValue()));
        } else {
            node.getStatements().forEach(statement -> statement.accept(this));
            currentFunction.getReturnBlock().setEscapeInstruction(new IRReturnInstruction(returnIRType, null));
        }
        if (!currentBasicBlock.hasEscapeInstruction()) currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, currentFunction.getReturnBlock(), null, currentBasicBlock));
        // don't inherit any encountered flow mark since function has ended
        currentScope = currentScope.getParentScope();
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentFunction.finishFunction();
        currentBasicBlock = null;
        currentFunction = null;
    }

    @Override
    public void visit(ParameterDefineNode node) {

    }

    @Override
    public void visit(BlockStatementNode node) {
        if (currentScope.hasEncounteredFlow()) return;
        currentScope = currentScope.getBlockScope(node.getScopeId());
        node.getStatements().forEach(statement -> statement.accept(this));
        currentScope.getParentScope().inheritEncounteredFlowMark(currentScope);
        currentScope = currentScope.getParentScope();
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
        // don't inherit any encountered flow mark
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
            // don't inherit any encountered flow mark too
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
        } else conditionResult = new IRConstBool(getBoolType(), true);
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(conditionResult, bodyBlock, terminateBlock, currentBasicBlock));
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentBasicBlock = bodyBlock;
        LoopScope loopScope = currentScope.getLoopScope();
        loopScope.setLoopConditionBlock(conditionBlock);
        loopScope.setLoopTerminateBlock(terminateBlock);
        node.getLoopBody().accept(this);
        if (currentScope.hasEncounteredFlow()) {
            // only keep return mark
            currentScope.getParentScope().inheritEncounteredFlowMark(currentScope);
            currentScope.getParentScope().eraseEncounteredLoopFlowMark();
        } else {
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
        currentScope.getParentScope().inheritEncounteredFlowMark(currentScope);
        currentScope = currentScope.getParentScope();
        currentScope.eraseEncounteredLoopFlowMark();
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
        if (node.hasReturnValue()) {
            node.getReturnValue().accept(this);
            IROperand returnValueRegister = node.getReturnValue().getIRResultValue();
            // return value should be store in a specific IRRegister, which will be created at the
            // entry block, i.e., the first time visit FunctionDefineNode, and escape block return that register.
            currentBasicBlock.appendInstruction(new IRStoreInstruction(currentFunction.getReturnType(), currentScope.getReturnValuePtr(), returnValueRegister));
        }
        // return statement should create an escape (i.e., branch) instruction to returnBlock.
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, currentFunction.getReturnBlock(), null, currentBasicBlock));
        currentScope.setAsEncounteredFlow(Scope.flowStatementType.returnType);
    }

    @Override
    public void visit(BreakStatementNode node) {
        if (currentScope.hasEncounteredFlow()) return;
        LoopScope loopScope = currentScope.getLoopScope();
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, loopScope.getLoopTerminateBlock(), null, currentBasicBlock));
        currentScope.setAsEncounteredFlow(Scope.flowStatementType.breakType);
    }

    @Override
    public void visit(ContinueStatementNode node) {
        if (currentScope.hasEncounteredFlow()) return;
        LoopScope loopScope = currentScope.getLoopScope();
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, loopScope.getLoopConditionBlock(), null, currentBasicBlock));
        currentScope.setAsEncounteredFlow(Scope.flowStatementType.continueType);
    }

    @Override
    public void visit(ExpressionStatementNode node) {
        if (currentScope.hasEncounteredFlow()) return;
        node.getExpression().accept(this);
    }

    @Override
    public void visit(EmptyStatementNode node) {

    }

    private final LinkedList<IROperand> newArrayDimensionLength = new LinkedList<>();

    private IRRegister generateNewArray(IRTypeSystem elementType, int posOnArray) {
        IROperand length = newArrayDimensionLength.get(posOnArray);
        IRRegister arrayLength = new IRRegister(getIntType());
        // length of array
        currentBasicBlock.appendInstruction(new IRBinaryInstruction("mul", arrayLength, length, new IRConstInt(getIntType(), elementType.sizeof())));
        IRRegister mallocSize = new IRRegister(getIntType());
        // 4 byte for address to array length
        currentBasicBlock.appendInstruction(new IRBinaryInstruction("add", mallocSize, arrayLength, new IRConstInt(getIntType(), 4)));
        IRFunction malloc = module.getBuiltinFunction("__mx_malloc");
        IRRegister mallocPtr = new IRRegister(getStringType());
        IRCallInstruction callInst = new IRCallInstruction(getStringType(), malloc);
        callInst.addArgument(mallocSize, getIntType());
        callInst.setResultRegister(mallocPtr);
        currentBasicBlock.appendInstruction(callInst);
        IRRegister arrayLengthRegister = new IRRegister(new IRPointerType(getIntType()));
        currentBasicBlock.appendInstruction(new IRBitcastInstruction(arrayLengthRegister, mallocPtr, new IRPointerType(getIntType())));
        currentBasicBlock.appendInstruction(new IRStoreInstruction(getIntType(), arrayLengthRegister, length));
        IRRegister arrayIntPtrRegister = new IRRegister(new IRPointerType(getIntType()));
        IRGetelementptrInstruction gepInst1 = new IRGetelementptrInstruction(arrayIntPtrRegister, getIntType(), arrayLengthRegister);
        gepInst1.addIndex(new IRConstInt(getIntType(), 1));
        currentBasicBlock.appendInstruction(gepInst1);
        IRRegister arrayRegister = new IRRegister(new IRPointerType(elementType));
        currentBasicBlock.appendInstruction(new IRBitcastInstruction(arrayRegister, arrayIntPtrRegister, new IRPointerType(elementType)));
        if (posOnArray == newArrayDimensionLength.size() - 1) return arrayRegister; // escape recursion
        assert elementType instanceof IRPointerType;
        // use for loop to generate each element of array
        int id = labelCnt++;
        IRBasicBlock conditionBlock = new IRBasicBlock(currentFunction, id + "_array_generation_for_condition");
        IRBasicBlock bodyBlock = new IRBasicBlock(currentFunction, id + "_array_generation_for_body");
        IRBasicBlock terminateBlock = new IRBasicBlock(currentFunction, id + "_array_generation_for_terminate");
        // for (auto iter = arr,
        IRTypeSystem arrayType = arrayRegister.getIRType();
        IRRegister iterPtr = new IRRegister(new IRPointerType(arrayType));
        currentBasicBlock.appendInstruction(new IRAllocaInstruction(arrayType, iterPtr));
        currentBasicBlock.appendInstruction(new IRStoreInstruction(arrayType, iterPtr, arrayRegister));
        // iterEnd = arr + length;
        IRRegister iterEnd = new IRRegister(arrayType);
        IRGetelementptrInstruction gepInst2 = new IRGetelementptrInstruction(iterEnd, elementType, arrayRegister);
        gepInst2.addIndex(length);
        currentBasicBlock.appendInstruction(gepInst2);
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, conditionBlock, null, currentBasicBlock));
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentBasicBlock = conditionBlock;
        // iter != iterEnd;
        IRRegister iterVal = new IRRegister(arrayType);
        currentBasicBlock.appendInstruction(new IRLoadInstruction(arrayType, iterVal, iterPtr));
        IRRegister conditionResult = new IRRegister(getBoolType());
        // using icmp to compare pointer
        currentBasicBlock.appendInstruction(new IRIcmpInstruction("ne", conditionResult, iterVal, iterEnd));
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(conditionResult, bodyBlock, terminateBlock, currentBasicBlock));
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentBasicBlock = bodyBlock;
        // loopBody: iter = new elementType[xxx];
        IRRegister currentIterInitVal = generateNewArray(((IRPointerType) elementType).getBaseType(), posOnArray + 1);
        currentBasicBlock.appendInstruction(new IRStoreInstruction(elementType, iterVal, currentIterInitVal));
        // iter++)
        IRRegister updatedIterVal = new IRRegister(arrayType);
        IRGetelementptrInstruction gepInst3 = new IRGetelementptrInstruction(updatedIterVal, elementType, iterVal);
        gepInst3.addIndex(new IRConstInt(getIntType(), 1));
        currentBasicBlock.appendInstruction(gepInst3);
        currentBasicBlock.appendInstruction(new IRStoreInstruction(arrayType, iterPtr, updatedIterVal));
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, conditionBlock, null, currentBasicBlock));
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
        currentBasicBlock = terminateBlock;
        return arrayRegister;
    }

    @Override
    public void visit(NewTypeExpressionNode node) {
        if (node.isNewArray()) {
            IRTypeSystem elementType = IRPointerType.constructIRPointerType(node.getRootElementType().toIRType(module), node.getDimension() - 1);
            assert newArrayDimensionLength.isEmpty();
            node.getDimensionExpressions().forEach(expression -> {
                expression.accept(this);
                newArrayDimensionLength.addLast(expression.getIRResultValue());
            });
            IRRegister arrayRegister = generateNewArray(elementType, 0);
            newArrayDimensionLength.clear();
            node.setIRResultValue(arrayRegister);
        } else { // todo new class();

        }
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
            IRTypeSystem instanceType = node.getInstance().getExpressionType().toIRType(module);
            if (instanceType.isString()) {
                node.getInstance().accept(this);
                // support const string to invoke builtin method
                IROperand str = node.getInstance().getIRResultValue();
                IRTypeSystem returnType;
                String innerFunctionName;
                switch (node.getFunctionName()) {
                    case "length" -> {
                        returnType = getIntType();
                        innerFunctionName = "__mx_stringLength";
                    }
                    case "substring" -> {
                        returnType = getStringType();
                        innerFunctionName = "__mx_stringSubstring";
                    }
                    case "parseInt" -> {
                        returnType = getIntType();
                        innerFunctionName = "__mx_stringParseInt";
                    }
                    case "ord" -> {
                        returnType = getIntType();
                        innerFunctionName = "__mx_stringOrd";
                    }
                    default -> throw new IRError("undefined string method");
                }
                IRFunction function = module.getBuiltinFunction(innerFunctionName);
                IRCallInstruction callInst = new IRCallInstruction(returnType, function);
                callInst.addArgument(str, getStringType());
                node.getArguments().forEach(argument -> {
                    argument.accept(this);
                    callInst.addArgument(argument.getIRResultValue(), argument.getExpressionType().toIRType(module));
                });
                IRRegister resultRegister = new IRRegister(returnType);
                callInst.setResultRegister(resultRegister);
                node.setIRResultValue(resultRegister);
                currentBasicBlock.appendInstruction(callInst);
            } else if (instanceType.isArray()) {
                assert node.getArguments().size() == 0;
                assert Objects.equals(node.getFunctionName(), "size");
                node.getInstance().accept(this);
                assert node.getInstance().getIRResultValue() instanceof IRRegister;
                IRRegister array = ((IRRegister) node.getInstance().getIRResultValue());
                IRRegister intArray = new IRRegister(new IRPointerType(getIntType()));
                currentBasicBlock.appendInstruction(new IRBitcastInstruction(intArray, array, new IRPointerType(getIntType())));
                IRRegister sizePtr = new IRRegister(new IRPointerType(getIntType()));
                IRGetelementptrInstruction gepInst = new IRGetelementptrInstruction(sizePtr, getIntType(), intArray);
                gepInst.addIndex(new IRConstInt(getIntType(), -1));
                currentBasicBlock.appendInstruction(gepInst);
                IRRegister sizeRegister = new IRRegister(getIntType());
                currentBasicBlock.appendInstruction(new IRLoadInstruction(getIntType(), sizeRegister, sizePtr));
                node.setIRResultValue(sizeRegister);
            } else {
                // todo call class method

            }
        } else {
            // the most critical code to print Hello, Happy World!
            FunctionEntity entity = currentScope.getFunctionRecursively(node.getFunctionName());
            IRFunction function = entity.getIRFunction();
            IRCallInstruction callInst = new IRCallInstruction(node.getExpressionType().toIRType(module), function);
            node.getArguments().forEach(argument -> {
                argument.accept(this);
                callInst.addArgument(argument.getIRResultValue(), argument.getExpressionType().toIRType(module));
            });
            if (!entity.getReturnType().isVoid()) {
                IRRegister resultRegister = new IRRegister(entity.getReturnType().toIRType(module));
                callInst.setResultRegister(resultRegister);
                node.setIRResultValue(resultRegister);
            }
            currentBasicBlock.appendInstruction(callInst);
        }
    }

    @Override
    public void visit(AddressingExpressionNode node) {
        node.getIndex().accept(this);
        IROperand index = node.getIndex().getIRResultValue();
        node.getArray().accept(this);
        assert node.getArray().getIRResultValue() instanceof IRRegister;
        IRRegister array = (IRRegister) node.getArray().getIRResultValue();
        IRTypeSystem arrayType = array.getIRType();
        assert arrayType instanceof IRPointerType;
        IRTypeSystem elementType = ((IRPointerType) arrayType).getBaseType();
        IRRegister arrayIndexedPtr = new IRRegister(arrayType);
        IRGetelementptrInstruction gepInst = new IRGetelementptrInstruction(arrayIndexedPtr, elementType, array);
        gepInst.addIndex(index);
        currentBasicBlock.appendInstruction(gepInst);
        node.setLeftValuePointer(arrayIndexedPtr);
        IRRegister resultRegister = new IRRegister(elementType);
        currentBasicBlock.appendInstruction(new IRLoadInstruction(elementType, resultRegister, arrayIndexedPtr));
        node.setIRResultValue(resultRegister);
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
            currentBasicBlock.appendInstruction(new IRBinaryInstruction("add", resultRegister, new IRConstInt(getIntType(), Objects.equals(node.getOp(), "++") ? 1 : -1), lhsVal));
            currentBasicBlock.appendInstruction(new IRStoreInstruction(variableType, currentRegister, resultRegister));
            node.setIRResultValue(tempRegister);
        } else if (bottomLeftValueNode instanceof AddressingExpressionNode) {
            AddressingExpressionNode leftValNode = (AddressingExpressionNode) bottomLeftValueNode;
            IRRegister leftValPtr = leftValNode.getLeftValuePointer();
            currentBasicBlock.appendInstruction(new IRLoadInstruction(resultType, tempRegister, leftValPtr));
            IRTypeSystem addrExpType = leftValNode.getExpressionType().toIRType(module);
            currentBasicBlock.appendInstruction(new IRBinaryInstruction("add", resultRegister, new IRConstInt(getIntType(), Objects.equals(node.getOp(), "++") ? 1 : -1), lhsVal));
            currentBasicBlock.appendInstruction(new IRStoreInstruction(addrExpType, leftValPtr, resultRegister));
            node.setIRResultValue(tempRegister);
        } else {
            assert bottomLeftValueNode instanceof MemberAccessExpressionNode;
            // todo
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
            IRRegister rhsCharVal = new IRRegister(getCharType());
            currentBasicBlock.appendInstruction(new IRZextInstruction(rhsCharVal, rhsVal, getCharType()));
            IRRegister resultCharVal = new IRRegister(getCharType());
            currentBasicBlock.appendInstruction(new IRBinaryInstruction("xor", resultCharVal, new IRConstChar(getIntType(), 1), rhsCharVal));
            currentBasicBlock.appendInstruction(new IRTruncInstruction(resultRegister, resultCharVal, getBoolType()));
        } else {
            assert resultType.isInt();
            if (Objects.equals(node.getOp(), "++") || Objects.equals(node.getOp(), "--")) {
                ASTNode bottomLeftValueNode = node.getRhs().getBottomLeftValueNode();
                if (bottomLeftValueNode instanceof IdentifierPrimaryNode) {
                    VariableEntity entity = currentScope.getVariableEntityRecursively(((IdentifierPrimaryNode) bottomLeftValueNode).getIdentifier());
                    IRRegister currentRegister = entity.getCurrentRegister();
                    IRTypeSystem variableType = ((IdentifierPrimaryNode) bottomLeftValueNode).getExpressionType().toIRType(module);
                    currentBasicBlock.appendInstruction(new IRBinaryInstruction("add", resultRegister, new IRConstInt(getIntType(), Objects.equals(node.getOp(), "++") ? 1 : -1), rhsVal));
                    currentBasicBlock.appendInstruction(new IRStoreInstruction(variableType, currentRegister, resultRegister));
                    node.setIRResultValue(resultRegister);
                } else if (bottomLeftValueNode instanceof AddressingExpressionNode) {
                    AddressingExpressionNode leftValNode = (AddressingExpressionNode) bottomLeftValueNode;
                    IRRegister leftValPtr = leftValNode.getLeftValuePointer();
                    IRTypeSystem addrExpType = leftValNode.getExpressionType().toIRType(module);
                    currentBasicBlock.appendInstruction(new IRBinaryInstruction("add", resultRegister, new IRConstInt(getIntType(), Objects.equals(node.getOp(), "++") ? 1 : -1), rhsVal));
                    currentBasicBlock.appendInstruction(new IRStoreInstruction(addrExpType, leftValPtr, resultRegister));
                } else {
                    assert bottomLeftValueNode instanceof MemberAccessExpressionNode;
                    // todo
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
                currentBasicBlock.appendInstruction(new IRBinaryInstruction(op, resultRegister, new IRConstInt(getIntType(), lhsVal), rhsVal));
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
                IRRegister lhsCharVal = new IRRegister(getCharType());
                currentBasicBlock.appendInstruction(new IRZextInstruction(lhsCharVal, lhsVal, getCharType()));
                IRRegister rhsCharVal = new IRRegister(getCharType());
                currentBasicBlock.appendInstruction(new IRZextInstruction(rhsCharVal, rhsVal, getCharType()));
                IRRegister resultCharVal = new IRRegister(getCharType());
                IRRegister resultRegister = new IRRegister(resultType);
                currentBasicBlock.appendInstruction(new IRBinaryInstruction(Objects.equals(node.getOp(), "&&") ? "and" : "or", resultCharVal, lhsCharVal, rhsCharVal));
                currentBasicBlock.appendInstruction(new IRTruncInstruction(resultRegister, resultCharVal, getBoolType()));
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
                    IRFunction cmpFunction;
                    switch (node.getOp()) {
                        case "<" -> cmpFunction = module.getBuiltinFunction("__mx_stringLt");
                        case "<=" -> cmpFunction = module.getBuiltinFunction("__mx_stringLe");
                        case ">" -> cmpFunction = module.getBuiltinFunction("__mx_stringGt");
                        case ">=" -> cmpFunction = module.getBuiltinFunction("__mx_stringGe");
                        case "==" -> cmpFunction = module.getBuiltinFunction("__mx_stringEq");
                        case "!=" -> cmpFunction = module.getBuiltinFunction("__mx_stringNe");
                        default -> throw new IRError("invalid binary op");
                    }
                    IRRegister boolTempRegister = new IRRegister(getCharType());
                    IRCallInstruction inst = new IRCallInstruction(getCharType(), cmpFunction);
                    inst.addArgument(lhsVal, getStringType()).addArgument(rhsVal, getStringType());
                    inst.setResultRegister(boolTempRegister);
                    currentBasicBlock.appendInstruction(inst);
                    // trunc char return value to i1
                    IRRegister resultRegister = new IRRegister(resultType);
                    currentBasicBlock.appendInstruction(new IRTruncInstruction(resultRegister, boolTempRegister, getBoolType()));
                    node.setIRResultValue(resultRegister);
                } else {
                    IRRegister resultRegister = new IRRegister(getBoolType());
                    String op = Objects.equals(node.getOp(), "==") ? "eq" : "ne";
                    // using icmp to compare pointer
                    currentBasicBlock.appendInstruction(new IRIcmpInstruction(op, resultRegister, lhsVal, rhsVal));
                    node.setIRResultValue(resultRegister);
                }
            }
        } else { // resultType.isString
            assert resultType.isString();
            // manually call strcat in c to implement string +
            assert Objects.equals(node.getOp(), "+");
            IRRegister resultRegister = new IRRegister(resultType);
            IRFunction function = module.getBuiltinFunction("__mx_concatenateString");
            IRCallInstruction inst = new IRCallInstruction(getStringType(), function);
            inst.addArgument(lhsVal, getStringType()).addArgument(rhsVal, getStringType());
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
        } else if (bottomLeftValueNode instanceof AddressingExpressionNode) {
            AddressingExpressionNode leftValNode = (AddressingExpressionNode) bottomLeftValueNode;
            IRRegister leftValPtr = leftValNode.getLeftValuePointer();
            IRTypeSystem addrExpType = leftValNode.getExpressionType().toIRType(module);
            currentBasicBlock.appendInstruction(new IRStoreInstruction(addrExpType, leftValPtr, node.getRhs().getIRResultValue()));
        } else {
            assert bottomLeftValueNode instanceof MemberAccessExpressionNode;

        }
        node.setIRResultValue(node.getRhs().getIRResultValue());
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
        node.setIRResultValue(new IRConstInt(getIntType(), node.getNumericalConstant()));
    }

    @Override
    public void visit(BoolConstantPrimaryNode node) {
        node.setIRResultValue(new IRConstBool(getBoolType(), node.getBoolConstant()));
    }

    @Override
    public void visit(StringConstantPrimaryNode node) {
        node.setIRResultValue(new IRConstString(getStringType(), node.getStringConstant(), module.getConstStringId(node.getStringConstant())));
    }

    @Override
    public void visit(NullConstantPrimaryNode node) {
        node.setIRResultValue(new IRNull(getVoidType()));
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

package FrontEnd;

import AST.ASTNode;
import AST.ASTVisitor;
import AST.DefineNode.*;
import AST.ExpressionNode.*;
import AST.PrimaryNode.*;
import AST.ProgramNode;
import AST.StatementNode.*;
import AST.TypeNode.*;
import BackEnd.InstructionSelector;
import IR.IRBasicBlock;
import IR.IRFunction;
import IR.IRGlobalDefine;
import IR.IRModule;
import IR.Instruction.*;
import IR.Operand.*;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRStructureType;
import IR.TypeSystem.IRTypeSystem;
import Memory.Memory;
import Utility.Cursor;
import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.Scope.*;
import Utility.Type.ClassType;
import Utility.error.IRError;

import java.util.LinkedList;
import java.util.Objects;

/**
 * This class generate source code's
 * intermediate representation, and
 * support standard llvm ir output.
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

    static boolean build = false;

    public static void enable() {
        build = true;
    }

    public static void disable() {
        build = false;
    }

    /**
     * This method build ir structure
     * for source code and store it in
     * IRModule inside Memory.
     *
     * @see Memory
     */
    public void build(Memory memory) {
        if (build) {
            currentScope = globalScope = memory.getGlobalScope();
            module = memory.getIRModule();
            module.initializeBuiltinFunction(globalScope);
            memory.getASTRoot().accept(this);
            if (InstructionSelector.codegen()) module.relocationInitializeFunctions();
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

    public IRTypeSystem getNullType() {
        return module.getIRType("null");
    }

    private void appendInst(IRInstruction inst) {
        // alloca in entry block to avoid multi alloca inside loop
        // [[--NOTICE--]] alloca register cannot use temporary register (%0) now
        // since numerical order will be disrupted
        if (inst instanceof IRAllocaInstruction) currentFunction.getEntryBlock().appendAlloca((IRAllocaInstruction) inst);
        else currentBasicBlock.appendInstruction(inst);
    }

    private void finishCurrentBasicBlock() {
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
    }

    private void finishCurrentBasicBlock(IRInstruction escInst) {
        assert escInst instanceof IRBrInstruction || escInst instanceof IRReturnInstruction;
        currentBasicBlock.setEscapeInstruction(escInst);
        currentBasicBlock.finishBlock();
        currentFunction.appendBasicBlock(currentBasicBlock);
    }


    @Override
    public void visit(ProgramNode node) {
        // collect class
        node.getDefines().forEach(define -> {
            if (define instanceof ClassDefineNode) {
                String className = ((ClassDefineNode) define).getClassName();
                IRStructureType classType = new IRStructureType(className);
                module.addIRClassType(className, classType);
                globalScope.getClass(className).setClassIRType(classType);
            }
        });
        // collect class method and class member
        node.getDefines().forEach(define -> {
            if (define instanceof ClassDefineNode) {
                String className = ((ClassDefineNode) define).getClassName();
                assert module.getIRType(className) instanceof IRStructureType;
                IRStructureType classIRType = (IRStructureType) module.getIRType(className);
                ClassType classType = globalScope.getClass(className);
                if (((ClassDefineNode) define).hasCustomConstructor()) {
                    classIRType.setAsHasCustomConstructor();
                    // class constructor format: <className>.__mx_ctors
                    IRFunction constructor = new IRFunction(className + ".__mx_ctors");
                    constructor.setReturnType(getVoidType());
                    constructor.addParameter("__mx_thisPtr", new IRPointerType(classIRType));
                    VariableEntity thisPtrCtors = new VariableEntity(classType, "__mx_thisPtr", new Cursor(-1, -1));
                    classType.getClassScope().getConstructor().getConstructorScope().addVariable(thisPtrCtors);
                    module.addFunction(constructor);
                }
                ((ClassDefineNode) define).getMethods().forEach(method -> {
                    FunctionEntity functionEntity = classType.getClassScope().getFunction(method.getFunctionName());
                    functionEntity.getFunctionScope().getParameters().forEach(VariableEntity::setAsVisitedInIR);
                    // class method format: <className>.<methodName>
                    IRFunction function = new IRFunction(className + "." + method.getFunctionName());
                    function.setReturnType(method.getReturnType().toIRType(module));
                    IRRegister.reset();
                    // method's first parameter is this
                    function.addParameter("__mx_thisPtr", new IRPointerType(classIRType));
                    VariableEntity thisPtr = new VariableEntity(classType, "__mx_thisPtr", new Cursor(-1, -1));
                    globalScope.getClass(className).getClassScope().getFunction(method.getFunctionName()).getFunctionScope().addVariable(thisPtr);
                    method.getParameters().forEach(parameter -> function.addParameter(parameter.getParameterName(), parameter.getType().toIRType(module)));
                    module.addFunction(function);
                    globalScope.getClass(className).getClassScope().getFunction(method.getFunctionName()).setIRFunction(function);
                });
                ((ClassDefineNode) define).getMembers().forEach(memberDefine -> {
                    IRTypeSystem memberType = memberDefine.getType().toIRType(module);
                    memberDefine.getSingleDefines().forEach(member -> {
                        String memberName = member.getVariableNameStr();
                        int index = classIRType.addMember(memberName, memberType);
                        // for member access
                        VariableEntity memberEntity = classType.getClassScope().getVariableEntity(memberName);
                        memberEntity.addClassMemberInfo(classIRType, index);
                        memberEntity.setAsVisitedInIR();
                    });
                });
            }
        });

        // collect function
        node.getDefines().forEach(define -> {
            if (define instanceof FunctionDefineNode) {
                FunctionEntity functionEntity = currentScope.getFunction(((FunctionDefineNode) define).getFunctionName());
                functionEntity.getFunctionScope().getParameters().forEach(VariableEntity::setAsVisitedInIR);
                IRFunction function = new IRFunction(((FunctionDefineNode) define).getFunctionName());
                function.setReturnType(((FunctionDefineNode) define).getReturnType().toIRType(module));
                IRRegister.reset();
                ((FunctionDefineNode) define).getParameters().forEach(parameter -> function.addParameter(parameter.getParameterName(), parameter.getType().toIRType(module)));
                module.addFunction(function);
                globalScope.getFunctionRecursively(((FunctionDefineNode) define).getFunctionName()).setIRFunction(function);
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

        node.getDefines().forEach(define -> {
            if (define instanceof ClassDefineNode) define.accept(this);
        });

        // step into functions
        node.getDefines().forEach(define -> {
            if (define instanceof FunctionDefineNode) define.accept(this);
        });
    }

    @Override
    public void visit(ClassDefineNode node) {
        currentScope = globalScope.getClass(node.getClassName()).getClassScope();
        if (node.hasCustomConstructor()) node.getConstructor().accept(this);
        node.getMethods().forEach(method -> method.accept(this));
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
        currentScope.getVariableEntity(node.getVariableNameStr()).setAsVisitedInIR();
        if (currentScope instanceof GlobalScope) {
            assert currentFunction == null && currentBasicBlock == null;
            IRGlobalVariableRegister variableRegister = new IRGlobalVariableRegister(new IRPointerType(variableIRType), node.getVariableNameStr());
            IRGlobalDefine define = new IRGlobalDefine(node.getVariableNameStr(), node.getType().toIRType(module));
            currentScope.getVariableEntityRecursively(node.getVariableNameStr()).setCurrentRegister(variableRegister);
            if (node.hasInitializeValue()) {
                // generate an initialize function
                IRFunction singleInitializeFunction = module.generateSingleInitializeFunction();
                currentFunction = singleInitializeFunction;
                currentBasicBlock = singleInitializeFunction.getEntryBlock();
                IRRegister.reset();
                IRRegister.resetAlloca();
                node.getInitializeValue().accept(this);
                // similar to ReturnStatementNode
                IROperand initializeValue = node.getInitializeValue().getIRResultValue();
                appendInst(new IRStoreInstruction(variableIRType, variableRegister, initializeValue));
                currentFunction.getReturnBlock().setEscapeInstruction(new IRReturnInstruction(getVoidType(), null));
                finishCurrentBasicBlock(new IRBrInstruction(null, currentFunction.getReturnBlock(), null, currentBasicBlock));
                currentFunction.finishFunction();
                currentFunction = null;
                currentBasicBlock = null;
            }
            module.addGlobalDefine(define);
        } else {
            IRRegister variableRegister;
            if (variableIRType.isClassPointer()) {
                if (node.hasInitializeValue()) {
                    variableRegister = new IRRegister(new IRPointerType(variableIRType), "alloca", true);
                    appendInst(new IRAllocaInstruction(variableIRType, variableRegister));
                    node.getInitializeValue().accept(this);
                    IROperand initVal = node.getInitializeValue().getIRResultValue();
                    appendInst(new IRStoreInstruction(variableIRType, variableRegister, initVal));
                } else {
                    IRRegister classCharPtr = new IRRegister(getStringType(), "class_malloc_ptr");
                    IRCallInstruction callInst = new IRCallInstruction(getStringType(), module.getBuiltinFunction("__mx_malloc"));
                    IRStructureType classIRType = (IRStructureType) ((IRPointerType) variableIRType).getBaseType();
                    callInst.addArgument(new IRConstInt(getIntType(), classIRType.sizeof()), getIntType());
                    callInst.setResultRegister(classCharPtr);
                    appendInst(callInst);
                    IRRegister classRegister = new IRRegister(variableIRType, "class_ptr");
                    appendInst(new IRBitcastInstruction(classRegister, classCharPtr, variableIRType));
                    variableRegister = new IRRegister(new IRPointerType(variableIRType), "alloca", true);
                    appendInst(new IRAllocaInstruction(variableIRType, variableRegister));
                    appendInst(new IRStoreInstruction(variableIRType, variableRegister, classRegister));
                }
            } else {
                variableRegister = new IRRegister(new IRPointerType(variableIRType), "alloca", true);
                appendInst(new IRAllocaInstruction(variableIRType, variableRegister));
                IROperand initVal;
                if (node.hasInitializeValue()) {
                    node.getInitializeValue().accept(this);
                    initVal = node.getInitializeValue().getIRResultValue();
                } else {
                    initVal = variableIRType.getDefaultValue();
                }
                appendInst(new IRStoreInstruction(variableIRType, variableRegister, initVal));
            }
            currentScope.getVariableEntityRecursively(node.getVariableNameStr()).setCurrentRegister(variableRegister);
        }
    }

    @Override
    public void visit(ConstructorDefineNode node) {
        currentScope = ((ClassScope) currentScope).getConstructor().getConstructorScope();
        String constructorName = node.getConstructorName() + ".__mx_ctors";
        currentFunction = module.getFunction(constructorName);
        currentBasicBlock = currentFunction.getEntryBlock();
        // manage this parameter
        assert currentFunction.getParameterType().size() == 1;
        IRRegister.resetTo(1);
        IRTypeSystem parameterType = currentFunction.getParameterType().get(0);
        IRRegister parameterRegister = new IRRegister(new IRPointerType(parameterType), "para", true);
        ((MethodScope) currentScope).setThisPtrRegister(parameterRegister);
        VariableEntity parameterEntity = currentScope.getVariableEntityRecursively(currentFunction.getParameterName().get(0));
        parameterEntity.setCurrentRegister(parameterRegister);
        appendInst(new IRAllocaInstruction(parameterType, parameterRegister));
        appendInst(new IRStoreInstruction(parameterType, parameterRegister, currentFunction.getParameters().get(0)));
        node.getStatements().forEach(statement -> statement.accept(this));
        currentFunction.getReturnBlock().setEscapeInstruction(new IRReturnInstruction(getVoidType(), null));
        if (!currentBasicBlock.hasEscapeInstruction()) currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, currentFunction.getReturnBlock(), null, currentBasicBlock));
        // don't inherit any encountered flow mark since method has ended
        currentScope = currentScope.getParentScope();
        finishCurrentBasicBlock();
        currentFunction.finishFunction();
        currentBasicBlock = null;
        currentFunction = null;
    }

    @Override
    public void visit(FunctionDefineNode node) {
        String functionName;
        boolean insideClass = currentScope.insideClass();
        if (insideClass) functionName = currentScope.getInsideClassName() + "." + node.getFunctionName();
        else functionName = node.getFunctionName();
        currentFunction = module.getFunction(functionName);
        currentBasicBlock = currentFunction.getEntryBlock();
        currentScope = currentScope.getFunctionRecursively(node.getFunctionName()).getFunctionScope();
        int parameterNumber = currentFunction.getParameterType().size();
        IRRegister.resetTo(parameterNumber);
        for (int i = 0; i < parameterNumber; i++) {
            IRTypeSystem parameterType = currentFunction.getParameterType().get(i);
            IRRegister parameterRegister = new IRRegister(new IRPointerType(parameterType), "para", true);
            if (insideClass && i == 0) {
                // store this ptr
                ((MethodScope) currentScope).setThisPtrRegister(parameterRegister);
            }
            VariableEntity parameterEntity = currentScope.getVariableEntityRecursively(currentFunction.getParameterName().get(i));
            parameterEntity.setCurrentRegister(parameterRegister);
            appendInst(new IRAllocaInstruction(parameterType, parameterRegister));
        }
        for (int i = 0; i < parameterNumber; i++) {
            IRTypeSystem parameterType = currentFunction.getParameterType().get(i);
            VariableEntity parameterEntity = currentScope.getVariableEntityRecursively(currentFunction.getParameterName().get(i));
            IRRegister targetRegister = parameterEntity.getCurrentRegister();
            IRRegister srcRegister = currentFunction.getParameters().get(i);
            appendInst(new IRStoreInstruction(parameterType, targetRegister, srcRegister));
            if (i == 0) currentFunction.setThisRegister(targetRegister);
        }
        IRTypeSystem returnIRType = node.getReturnType().toIRType(module);
        if (!returnIRType.isVoid()) {
            IRRegister returnValuePtr = new IRRegister(new IRPointerType(returnIRType), "ret_val_ptr", true);
            appendInst(new IRAllocaInstruction(returnIRType, returnValuePtr));
            ((FunctionScope) currentScope).setReturnValuePtr(returnValuePtr);
            node.getStatements().forEach(statement -> statement.accept(this));
            // main function could have no return statement
            if (((FunctionScope) currentScope).hasReturnStatement()) {
                IRRegister returnValue = new IRRegister(returnIRType, "ret_val");
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
        finishCurrentBasicBlock();
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
        finishCurrentBasicBlock(new IRBrInstruction(node.getConditionExpression().getIRResultValue(), thenBlock, node.hasElse() ? elseBlock : terminateBlock, currentBasicBlock));
        currentBasicBlock = thenBlock;
        currentScope = currentScope.getBlockScope(node.getScopeId());
        node.getTrueStatement().accept(this);
        // don't inherit any encountered flow mark
        currentScope = currentScope.getParentScope();
        // everytime encountered a statement, need to check whether it has escape or not
        if (!currentBasicBlock.hasEscapeInstruction()) // return statement might generate an escape instruction
            currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, terminateBlock, null, currentBasicBlock));
        finishCurrentBasicBlock();
        if (node.hasElse()) {
            currentBasicBlock = elseBlock;
            currentScope = currentScope.getBlockScope(node.getIfElseId());
            node.getFalseStatement().accept(this);
            // don't inherit any encountered flow mark too
            currentScope = currentScope.getParentScope();
            if (!currentBasicBlock.hasEscapeInstruction()) // return statement might always generate an escape instruction
                currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, terminateBlock, null, currentBasicBlock));
            finishCurrentBasicBlock();
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
        IRBasicBlock stepBlock = node.hasStepExpression() ? new IRBasicBlock(currentFunction, id + "_for_step") : null;
        IRBasicBlock terminateBlock = new IRBasicBlock(currentFunction, id + "_for_terminate");
        if (node.hasInitializeStatement()) node.getInitializeStatement().accept(this);
        finishCurrentBasicBlock(new IRBrInstruction(null, conditionBlock, null, currentBasicBlock));
        currentBasicBlock = conditionBlock;
        IROperand conditionResult;
        if (node.hasConditionExpression()) {
            node.getConditionExpression().accept(this);
            conditionResult = node.getConditionExpression().getIRResultValue();
        } else conditionResult = new IRConstBool(getBoolType(), true);
        finishCurrentBasicBlock(new IRBrInstruction(conditionResult, bodyBlock, terminateBlock, currentBasicBlock));
        currentBasicBlock = bodyBlock;
        LoopScope loopScope = currentScope.getLoopScope();
        loopScope.setLoopConditionBlock(conditionBlock);
        loopScope.setLoopStepBlock(stepBlock);
        loopScope.setLoopTerminateBlock(terminateBlock);
        node.getLoopBody().accept(this);
        if (currentScope.hasEncounteredFlow()) {
            // only keep return mark
            currentScope.getParentScope().inheritEncounteredFlowMark(currentScope);
            currentScope.getParentScope().eraseEncounteredLoopFlowMark();
        } else {
            if (node.hasStepExpression()) {
                assert stepBlock != null;
                finishCurrentBasicBlock(new IRBrInstruction(null, stepBlock, null, currentBasicBlock));
                currentBasicBlock = stepBlock;
                node.getStepExpression().accept(this);
            }
            currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, conditionBlock, null, currentBasicBlock));
        }
        currentScope = currentScope.getParentScope();
        finishCurrentBasicBlock();
        currentBasicBlock = terminateBlock;
    }

    @Override
    public void visit(WhileStatementNode node) {
        if (currentScope.hasEncounteredFlow()) return;
        int id = labelCnt++;
        IRBasicBlock conditionBlock = new IRBasicBlock(currentFunction, id + "_while_condition");
        IRBasicBlock bodyBlock = new IRBasicBlock(currentFunction, id + "_while_body");
        IRBasicBlock terminateBlock = new IRBasicBlock(currentFunction, id + "_while_terminate");
        finishCurrentBasicBlock(new IRBrInstruction(null, conditionBlock, null, currentBasicBlock));
        currentBasicBlock = conditionBlock;
        node.getConditionExpression().accept(this);
        IROperand conditionResult = node.getConditionExpression().getIRResultValue();
        finishCurrentBasicBlock(new IRBrInstruction(conditionResult, bodyBlock, terminateBlock, currentBasicBlock));
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
        finishCurrentBasicBlock();
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
            appendInst(new IRStoreInstruction(currentFunction.getReturnType(), currentScope.getReturnValuePtr(), returnValueRegister));
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
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, loopScope.getContinueTarget(), null, currentBasicBlock));
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
        IRRegister arrayLength = new IRRegister(getIntType(), "arr_size");
        // length of array
        appendInst(new IRBinaryInstruction("mul", arrayLength, length, new IRConstInt(getIntType(), elementType.sizeof())));
        IRRegister mallocSize = new IRRegister(getIntType(), "malloc_size");
        // 4 byte for address to array length
        appendInst(new IRBinaryInstruction("add", mallocSize, arrayLength, new IRConstInt(getIntType(), 4)));
        IRFunction malloc = module.getBuiltinFunction("__mx_malloc");
        IRRegister mallocPtr = new IRRegister(getStringType(), "arr_malloc_ptr");
        IRCallInstruction callInst = new IRCallInstruction(getStringType(), malloc);
        callInst.addArgument(mallocSize, getIntType());
        callInst.setResultRegister(mallocPtr);
        appendInst(callInst);
        IRRegister arrayLengthRegister = new IRRegister(new IRPointerType(getIntType()), "arr_len");
        appendInst(new IRBitcastInstruction(arrayLengthRegister, mallocPtr, new IRPointerType(getIntType())));
        appendInst(new IRStoreInstruction(getIntType(), arrayLengthRegister, length));
        IRRegister arrayCharPtrRegister = new IRRegister(new IRPointerType(getCharType()), "arr_char_ptr");
        IRGetelementptrInstruction gepInst1 = new IRGetelementptrInstruction(arrayCharPtrRegister, getCharType(), mallocPtr);
        gepInst1.addIndex(new IRConstInt(getIntType(), 4));
        appendInst(gepInst1);
        IRRegister arrayRegister = new IRRegister(new IRPointerType(elementType), "arr_ptr");
        appendInst(new IRBitcastInstruction(arrayRegister, arrayCharPtrRegister, new IRPointerType(elementType)));
        if (posOnArray == newArrayDimensionLength.size() - 1) return arrayRegister; // escape recursion
        assert elementType instanceof IRPointerType;
        // use for loop to generate each element of array
        int id = labelCnt++;
        IRBasicBlock conditionBlock = new IRBasicBlock(currentFunction, id + "_array_generation_for_condition");
        IRBasicBlock bodyBlock = new IRBasicBlock(currentFunction, id + "_array_generation_for_body");
        IRBasicBlock terminateBlock = new IRBasicBlock(currentFunction, id + "_array_generation_for_terminate");
        // for (auto iter = arr,
        IRTypeSystem arrayType = arrayRegister.getIRType();
        IRRegister iterPtr = new IRRegister(new IRPointerType(arrayType), "arr_gen_iter", true);
        appendInst(new IRAllocaInstruction(arrayType, iterPtr));
        appendInst(new IRStoreInstruction(arrayType, iterPtr, arrayRegister));
        // iterEnd = arr + length;
        IRRegister iterEnd = new IRRegister(arrayType, "iter_end");
        IRGetelementptrInstruction gepInst2 = new IRGetelementptrInstruction(iterEnd, elementType, arrayRegister);
        gepInst2.addIndex(length);
        appendInst(gepInst2);
        finishCurrentBasicBlock(new IRBrInstruction(null, conditionBlock, null, currentBasicBlock));
        currentBasicBlock = conditionBlock;
        // iter != iterEnd;
        IRRegister iterVal = new IRRegister(arrayType, "iter");
        appendInst(new IRLoadInstruction(arrayType, iterVal, iterPtr));
        IRRegister conditionResult = new IRRegister(getBoolType(), "cond_res");
        // using icmp to compare pointer
        appendInst(new IRIcmpInstruction("ne", conditionResult, iterVal, iterEnd));
        finishCurrentBasicBlock(new IRBrInstruction(conditionResult, bodyBlock, terminateBlock, currentBasicBlock));
        currentBasicBlock = bodyBlock;
        // loopBody: iter = new elementType[xxx];
        IRRegister currentIterInitVal = generateNewArray(((IRPointerType) elementType).getBaseType(), posOnArray + 1);
        appendInst(new IRStoreInstruction(elementType, iterVal, currentIterInitVal));
        // iter++)
        IRRegister updatedIterVal = new IRRegister(arrayType, "upd_iter");
        IRGetelementptrInstruction gepInst3 = new IRGetelementptrInstruction(updatedIterVal, elementType, iterVal);
        gepInst3.addIndex(new IRConstInt(getIntType(), 1));
        appendInst(gepInst3);
        appendInst(new IRStoreInstruction(arrayType, iterPtr, updatedIterVal));
        finishCurrentBasicBlock(new IRBrInstruction(null, conditionBlock, null, currentBasicBlock));
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
        } else {
            IRPointerType classTypePtr = (IRPointerType) node.getRootElementType().toIRType(module);
            IRStructureType classType = (IRStructureType) classTypePtr.getBaseType();
            IRRegister newClassCharPtr = new IRRegister(getStringType(), "new_class_malloc_ptr");
            IRCallInstruction callInst = new IRCallInstruction(getStringType(), module.getBuiltinFunction("__mx_malloc"));
            callInst.addArgument(new IRConstInt(getIntType(), classType.sizeof()), getIntType());
            callInst.setResultRegister(newClassCharPtr);
            appendInst(callInst);
            IRRegister newClassVariable = new IRRegister(classTypePtr, "new_class_ptr");
            appendInst(new IRBitcastInstruction(newClassVariable, newClassCharPtr, classTypePtr));
            if (classType.hasCustomConstructor()) {
                IRFunction constructor = module.getFunction(node.getRootElementType().getTypeName() + ".__mx_ctors");
                IRCallInstruction callConstructorInst = new IRCallInstruction(getVoidType(), constructor);
                callConstructorInst.addArgument(newClassVariable, classTypePtr);
                appendInst(callConstructorInst);
            }
            node.setIRResultValue(newClassVariable);
        }
    }

    @Override
    public void visit(MemberAccessExpressionNode node) {
        assert !node.isAccessMethod();
        IRPointerType classTypePtr = (IRPointerType) node.getInstance().getExpressionType().toIRType(module);
        IRStructureType classIRType = (IRStructureType) classTypePtr.getBaseType();
        node.getInstance().accept(this);
        IRRegister classPtr = (IRRegister) node.getInstance().getIRResultValue();
        IRTypeSystem memberType = node.getExpressionType().toIRType(module);
        IRRegister classMemberPtr = new IRRegister(new IRPointerType(memberType), "class_member_ptr");
        int index = classIRType.getMemberIndex(node.getMemberName());
        IRGetelementptrInstruction gepInst = new IRGetelementptrInstruction(classMemberPtr, classIRType, classPtr);
        gepInst.addIndex(new IRConstInt(getIntType(), 0));
        gepInst.addIndex(new IRConstInt(getIntType(), index));
        appendInst(gepInst);
        IRRegister classMember = new IRRegister(memberType, "lass_member");
        appendInst(new IRLoadInstruction(memberType, classMember, classMemberPtr));
        node.setIRResultValue(classMember);
        node.setLeftValuePointer(classMemberPtr);
    }

    @Override
    public void visit(LambdaExpressionNode node) {

    }

    @Override
    public void visit(FunctionCallExpressionNode node) {
        IRFunction function;
        IRCallInstruction callInst;
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
                function = module.getBuiltinFunction(innerFunctionName);
                callInst = new IRCallInstruction(returnType, function);
                callInst.addArgument(str, getStringType());
                node.getArguments().forEach(argument -> {
                    argument.accept(this);
                    callInst.addArgument(argument.getIRResultValue(), argument.getExpressionType().toIRType(module));
                });
                IRRegister resultRegister = new IRRegister(returnType, "call_res");
                callInst.setResultRegister(resultRegister);
                node.setIRResultValue(resultRegister);
                appendInst(callInst);
                return;
            } else if (instanceType.isArray()) {
                assert node.getArguments().size() == 0;
                assert Objects.equals(node.getFunctionName(), "size");
                node.getInstance().accept(this);
                assert node.getInstance().getIRResultValue() instanceof IRRegister;
                IRRegister array = ((IRRegister) node.getInstance().getIRResultValue());
                IRRegister intArray = new IRRegister(new IRPointerType(getIntType()), "arr_int");
                appendInst(new IRBitcastInstruction(intArray, array, new IRPointerType(getIntType())));
                IRRegister sizePtr = new IRRegister(new IRPointerType(getIntType()), "arr_len_ptr");
                IRGetelementptrInstruction gepInst = new IRGetelementptrInstruction(sizePtr, getIntType(), intArray);
                gepInst.addIndex(new IRConstInt(getIntType(), -1));
                appendInst(gepInst);
                IRRegister sizeRegister = new IRRegister(getIntType(), "arr_len");
                node.setIRResultValue(sizeRegister);
                appendInst(new IRLoadInstruction(getIntType(), sizeRegister, sizePtr));
                return;
            } else {
                IRStructureType methodClass = (IRStructureType) ((IRPointerType) instanceType).getBaseType();
                function = module.getFunction(methodClass.getClassName() + "." + node.getFunctionName());
                callInst = new IRCallInstruction(node.getExpressionType().toIRType(module), function);
                node.getInstance().accept(this);
                callInst.addArgument(node.getInstance().getIRResultValue(), node.getInstance().getIRResultValue().getIRType());
            }
        } else {
            // the most critical code to print Hello, Happy World!
            function = currentScope.getFunctionRecursively(node.getFunctionName()).getIRFunction();
            callInst = new IRCallInstruction(node.getExpressionType().toIRType(module), function);
            // inside class: classMethod() -> this.classMethod()
            if (!Objects.equals(function.getFunctionName(), node.getFunctionName())) {
                ThisPrimaryNode thisPtrNode = new ThisPrimaryNode(new Cursor(-1, -1));
                thisPtrNode.accept(this);
                callInst.addArgument(thisPtrNode.getIRResultValue(), thisPtrNode.getIRResultValue().getIRType());
            }
        }
        node.getArguments().forEach(argument -> {
            argument.accept(this);
            callInst.addArgument(argument.getIRResultValue(), argument.getExpressionType().toIRType(module));
        });
        if (!function.getReturnType().isVoid()) {
            IRRegister resultRegister = new IRRegister(function.getReturnType(), "call_res");
            callInst.setResultRegister(resultRegister);
            node.setIRResultValue(resultRegister);
        }
        appendInst(callInst);
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
        IRRegister arrayIndexedPtr = new IRRegister(arrayType, "acss_ptr");
        IRGetelementptrInstruction gepInst = new IRGetelementptrInstruction(arrayIndexedPtr, elementType, array);
        gepInst.addIndex(index);
        appendInst(gepInst);
        node.setLeftValuePointer(arrayIndexedPtr);
        IRRegister resultRegister = new IRRegister(elementType, "acss_ele");
        appendInst(new IRLoadInstruction(elementType, resultRegister, arrayIndexedPtr));
        node.setIRResultValue(resultRegister);
    }

    IRRegister getIdentifierPtr(IdentifierPrimaryNode variableIdentifier) {
        VariableEntity entity = currentScope.getDefinedVariableEntityRecursively(variableIdentifier.getIdentifier());
        if (!entity.isClassMember()) return entity.getCurrentRegister();
        int index = entity.getIndex();
        IRStructureType classIRType = entity.getClassIRType();
        IRTypeSystem identifierType = entity.getVariableType().toIRType(module);
        assert currentScope.insideMethod();
        MethodScope methodScope = currentScope.getMethodScope();
        IRRegister thisPtrPtrRegister = methodScope.getThisPtrRegister();
        IRRegister thisPtrRegister = new IRRegister(new IRPointerType(classIRType), "this_ptr");
        appendInst(new IRLoadInstruction(new IRPointerType(classIRType), thisPtrRegister, thisPtrPtrRegister));
        IRRegister identifierPtr = new IRRegister(new IRPointerType(identifierType), "identifier_ptr");
        IRGetelementptrInstruction gepInst = new IRGetelementptrInstruction(identifierPtr, classIRType, thisPtrRegister);
        gepInst.addIndex(new IRConstInt(getIntType(), 0));
        gepInst.addIndex(new IRConstInt(getIntType(), index));
        appendInst(gepInst);
        return identifierPtr;
    }

    @Override
    public void visit(PostCrementExpressionNode node) {
        node.getLhs().accept(this);
        IROperand lhsVal = node.getLhs().getIRResultValue();
        IRTypeSystem resultType = node.getExpressionType().toIRType(module);
        assert resultType.isInt();
        IRRegister tempRegister, resultRegister;
        ASTNode bottomLeftValueNode = node.getLhs().getBottomLeftValueNode();
        if (bottomLeftValueNode instanceof IdentifierPrimaryNode) {
            IRRegister currentRegister = getIdentifierPtr(((IdentifierPrimaryNode) bottomLeftValueNode));
            tempRegister = new IRRegister(resultType, "temp");
            resultRegister = new IRRegister(resultType, "res");
            appendInst(new IRLoadInstruction(resultType, tempRegister, currentRegister));
            IRTypeSystem variableType = ((IdentifierPrimaryNode) bottomLeftValueNode).getExpressionType().toIRType(module);
            appendInst(new IRBinaryInstruction("add", resultRegister, new IRConstInt(getIntType(), Objects.equals(node.getOp(), "++") ? 1 : -1), lhsVal));
            appendInst(new IRStoreInstruction(variableType, currentRegister, resultRegister));
            node.setIRResultValue(tempRegister);
        } else {
            tempRegister = new IRRegister(resultType, "temp");
            resultRegister = new IRRegister(resultType, "res");
            assert bottomLeftValueNode instanceof LeftValueExpressionNode;
            LeftValueExpressionNode leftValNode = (LeftValueExpressionNode) bottomLeftValueNode;
            IRRegister leftValPtr = leftValNode.getLeftValuePointer();
            appendInst(new IRLoadInstruction(resultType, tempRegister, leftValPtr));
            IRTypeSystem addrExpType = leftValNode.getExpressionType().toIRType(module);
            appendInst(new IRBinaryInstruction("add", resultRegister, new IRConstInt(getIntType(), Objects.equals(node.getOp(), "++") ? 1 : -1), lhsVal));
            appendInst(new IRStoreInstruction(addrExpType, leftValPtr, resultRegister));
            node.setIRResultValue(tempRegister);
        }
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        node.getRhs().accept(this);
        IROperand rhsVal = node.getRhs().getIRResultValue();
        IRTypeSystem resultType = node.getExpressionType().toIRType(module);
        IRRegister resultRegister;
        if (resultType.isBool()) {
            assert Objects.equals(node.getOp(), "!");
            IRRegister rhsCharVal = new IRRegister(getCharType(), "rhs_char");
            appendInst(new IRZextInstruction(rhsCharVal, rhsVal, getCharType()));
            IRRegister resultCharVal = new IRRegister(getCharType(), "res_char");
            resultRegister = new IRRegister(resultType, "res");
            appendInst(new IRBinaryInstruction("xor", resultCharVal, new IRConstChar(getCharType(), 1), rhsCharVal));
            appendInst(new IRTruncInstruction(resultRegister, resultCharVal, getBoolType()));
        } else {
            assert resultType.isInt();
            if (Objects.equals(node.getOp(), "++") || Objects.equals(node.getOp(), "--")) {
                ASTNode bottomLeftValueNode = node.getRhs().getBottomLeftValueNode();
                if (bottomLeftValueNode instanceof IdentifierPrimaryNode) {
                    IRRegister currentRegister = getIdentifierPtr(((IdentifierPrimaryNode) bottomLeftValueNode));
                    resultRegister = new IRRegister(resultType, "res");
                    IRTypeSystem variableType = ((IdentifierPrimaryNode) bottomLeftValueNode).getExpressionType().toIRType(module);
                    appendInst(new IRBinaryInstruction("add", resultRegister, new IRConstInt(getIntType(), Objects.equals(node.getOp(), "++") ? 1 : -1), rhsVal));
                    appendInst(new IRStoreInstruction(variableType, currentRegister, resultRegister));
                    node.setIRResultValue(resultRegister);
                } else {
                    assert bottomLeftValueNode instanceof LeftValueExpressionNode;
                    LeftValueExpressionNode leftValNode = (LeftValueExpressionNode) bottomLeftValueNode;
                    IRRegister leftValPtr = leftValNode.getLeftValuePointer();
                    IRTypeSystem addrExpType = leftValNode.getExpressionType().toIRType(module);
                    resultRegister = new IRRegister(resultType, "res");
                    appendInst(new IRBinaryInstruction("add", resultRegister, new IRConstInt(getIntType(), Objects.equals(node.getOp(), "++") ? 1 : -1), rhsVal));
                    appendInst(new IRStoreInstruction(addrExpType, leftValPtr, resultRegister));
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
                        lhsVal = -1;
                    }
                    default -> throw new IRError("invalid unary op");
                }
                resultRegister = new IRRegister(resultType, "res");
                appendInst(new IRBinaryInstruction(op, resultRegister, new IRConstInt(getIntType(), lhsVal), rhsVal));
            }
        }
        node.setIRResultValue(resultRegister);
    }

    @Override
    public void visit(BinaryExpressionNode node) {
        if (node.getExpressionType().isBool() && (Objects.equals(node.getOp(), "&&") || Objects.equals(node.getOp(), "||"))) {
            // logic short-circuit
            IRRegister logicResultCharPtr = new IRRegister(new IRPointerType(getCharType()), "logic_short_circuit_result", true);
            appendInst(new IRAllocaInstruction(getCharType(), logicResultCharPtr));
            int id = labelCnt++;
            String opName = Objects.equals(node.getOp(), "&&") ? "and" : "or";
            IRBasicBlock shortCircuitBlock = new IRBasicBlock(currentFunction, id + "_" + opName + "_logic_short_circuit");
            IRBasicBlock nonShortCircuitBlock = new IRBasicBlock(currentFunction, id + "_" + opName + "_logic_non_short_circuit");
            IRBasicBlock terminateBlock = new IRBasicBlock(currentFunction, id + "_" + opName + "_logic_terminate");
            node.getLhs().accept(this);
            IROperand lhsVal = node.getLhs().getIRResultValue();
            finishCurrentBasicBlock(
                    Objects.equals(node.getOp(), "&&")
                            ? new IRBrInstruction(lhsVal, nonShortCircuitBlock, shortCircuitBlock, currentBasicBlock)
                            : new IRBrInstruction(lhsVal, shortCircuitBlock, nonShortCircuitBlock, currentBasicBlock)
            );
            currentBasicBlock = shortCircuitBlock;
            //  true || xxx -> short circuit
            // false && xxx -> short circuit
            boolean val = Objects.equals(node.getOp(), "||");
            IROperand logicShortCircuitResult = new IRConstBool(getBoolType(), val);
            IRRegister logicShortCircuitCharResult = new IRRegister(getCharType(), "char_res");
            appendInst(new IRZextInstruction(logicShortCircuitCharResult, logicShortCircuitResult, getCharType()));
            appendInst(new IRStoreInstruction(getCharType(), logicResultCharPtr, logicShortCircuitCharResult));
            finishCurrentBasicBlock(new IRBrInstruction(null, terminateBlock, null, currentBasicBlock));
            currentBasicBlock = nonShortCircuitBlock;
            node.getRhs().accept(this);
            IROperand rhsVal = node.getRhs().getIRResultValue();
            IRRegister lhsCharVal = new IRRegister(getCharType(), "lhs_char");
            appendInst(new IRZextInstruction(lhsCharVal, lhsVal, getCharType()));
            IRRegister rhsCharVal = new IRRegister(getCharType(), "rhs_char");
            appendInst(new IRZextInstruction(rhsCharVal, rhsVal, getCharType()));
            IRRegister resultCharVal = new IRRegister(getCharType(), "char_res");
            appendInst(new IRBinaryInstruction(Objects.equals(node.getOp(), "&&") ? "and" : "or", resultCharVal, lhsCharVal, rhsCharVal));
            appendInst(new IRStoreInstruction(getCharType(), logicResultCharPtr, resultCharVal));
            finishCurrentBasicBlock(new IRBrInstruction(null, terminateBlock, null, currentBasicBlock));
            currentBasicBlock = terminateBlock;
            IRRegister logicCharResult = new IRRegister(getCharType(), "char_res");
            appendInst(new IRLoadInstruction(getCharType(), logicCharResult, logicResultCharPtr));
            IRRegister logicResult = new IRRegister(getBoolType(), "bool_res");
            appendInst(new IRTruncInstruction(logicResult, logicCharResult, getBoolType()));
            node.setIRResultValue(logicResult);
            return;
        }
        node.getLhs().accept(this);
        node.getRhs().accept(this);
        IROperand lhsVal = node.getLhs().getIRResultValue();
        IROperand rhsVal = node.getRhs().getIRResultValue();
        IRTypeSystem resultType = node.getExpressionType().toIRType(module);
        if (resultType.isInt()) {
            IRRegister resultRegister = new IRRegister(resultType, "res");
            String op;
            switch (node.getOp()) {
                case "+" -> op = "add";
                case "-" -> op = "sub nsw"; // nsw stands for no signed wrap, which will set result to poison value if encounter overflow
                case "*" -> op = "mul";
                case "/" -> op = "sdiv"; // signed division
                case "%" -> op = "srem"; // signed remainder
                case "<<" -> op = "shl nsw";
                case ">>" -> op = "ashr";
                case "&" -> op = "and";
                case "^" -> op = "xor";
                case "|" -> op = "or";
                default -> throw new IRError("invalid binary op");
            }
            appendInst(new IRBinaryInstruction(op, resultRegister, lhsVal, rhsVal));
            node.setIRResultValue(resultRegister);
        } else if (resultType.isBool()) { // cmp since && and || has handled before
            if (node.getLhs().getExpressionType().isInt()) {
                IRRegister resultRegister = new IRRegister(resultType, "res");
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
                appendInst(new IRIcmpInstruction(op, resultRegister, lhsVal, rhsVal));
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
                IRRegister boolTempRegister = new IRRegister(getCharType(), "char_res");
                IRCallInstruction inst = new IRCallInstruction(getCharType(), cmpFunction);
                inst.addArgument(lhsVal, getStringType()).addArgument(rhsVal, getStringType());
                inst.setResultRegister(boolTempRegister);
                appendInst(inst);
                // trunc char return value to i1
                IRRegister resultRegister = new IRRegister(resultType, "bool_res");
                appendInst(new IRTruncInstruction(resultRegister, boolTempRegister, getBoolType()));
                node.setIRResultValue(resultRegister);
            } else {
                IRRegister resultRegister = new IRRegister(getBoolType(), "res");
                String op = Objects.equals(node.getOp(), "==") ? "eq" : "ne";
                // using icmp to compare pointer
                appendInst(new IRIcmpInstruction(op, resultRegister, lhsVal, rhsVal));
                node.setIRResultValue(resultRegister);
            }
        } else { // resultType.isString
            assert resultType.isString();
            // manually call strcat in c to implement string +
            assert Objects.equals(node.getOp(), "+");
            IRRegister resultRegister = new IRRegister(resultType, "res");
            IRFunction function = module.getBuiltinFunction("__mx_concatenateString");
            IRCallInstruction inst = new IRCallInstruction(getStringType(), function);
            inst.addArgument(lhsVal, getStringType()).addArgument(rhsVal, getStringType());
            inst.setResultRegister(resultRegister);
            appendInst(inst);
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
            IRRegister currentRegister = getIdentifierPtr(((IdentifierPrimaryNode) bottomLeftValueNode));
            IRTypeSystem variableType = ((IdentifierPrimaryNode) bottomLeftValueNode).getExpressionType().toIRType(module);
            appendInst(new IRStoreInstruction(variableType, currentRegister, node.getRhs().getIRResultValue()));
        } else {
            assert bottomLeftValueNode instanceof LeftValueExpressionNode;
            LeftValueExpressionNode leftValNode = (LeftValueExpressionNode) bottomLeftValueNode;
            IRRegister leftValPtr = leftValNode.getLeftValuePointer();
            IRTypeSystem addrExpType = leftValNode.getExpressionType().toIRType(module);
            appendInst(new IRStoreInstruction(addrExpType, leftValPtr, node.getRhs().getIRResultValue()));
        }
        node.setIRResultValue(node.getRhs().getIRResultValue());
    }

    @Override
    public void visit(ThisPrimaryNode node) {
        assert currentScope.insideClass();
        assert currentScope.insideMethod();
        IRRegister thisPtrPtr = currentFunction.getThisRegister();
        assert thisPtrPtr != null;
        IRTypeSystem thisPtrType = ((IRPointerType) thisPtrPtr.getIRType()).getBaseType();
        IRRegister thisPtr = new IRRegister(thisPtrType, "this_ptr");
        appendInst(new IRLoadInstruction(thisPtrType, thisPtr, thisPtrPtr));
        node.setIRResultValue(thisPtr);
    }

    @Override
    public void visit(IdentifierPrimaryNode node) {
        if (!node.isVariable()) throw new IRError("visit FunctionIdentifier in IRBuilder");
        IRRegister identifierPtr = getIdentifierPtr(node);
        IRTypeSystem identifierType = ((IRPointerType) identifierPtr.getIRType()).getBaseType();
        IRRegister loadTarget = new IRRegister(identifierType, "identifier");
        // global variables always use @identifier to store value, therefore
        // local variable value is stored in registers, and might change when updated
        // so load target need update to VariableEntity
        // access to local variable need load first, otherwise will cause type error
        appendInst(new IRLoadInstruction(identifierType, loadTarget, identifierPtr));
        node.setIRResultValue(loadTarget);
    }

    @Override
    public void visit(NumericalConstantPrimaryNode node) {
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
        node.setIRResultValue(new IRNull(getNullType()));
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

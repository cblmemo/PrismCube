package FrontEnd;

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
import Utility.Scope.FunctionScope;
import Utility.Scope.GlobalScope;
import Utility.Scope.Scope;
import Utility.error.IRError;

import java.util.Stack;

import static Debug.MemoLog.log;

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
    private IRBasicBlock currentBasicBlock;

    private enum status {
        idle, allocaParameter, storeParameter,
    }

    private final Stack<status> currentStatus = new Stack<>();

    public void build(Memory memory) {
        currentScope = globalScope = memory.getGlobalScope();
        module = memory.getIRModule();
        module.initializeBuiltinFunction(globalScope);
        currentStatus.push(status.idle);
        memory.getASTRoot().accept(this);
        currentStatus.pop();
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
        entryBlock.setEscapeInstruction(new IRBrInstruction(null, globalConstructor.getReturnBlock(), null, module));
        entryBlock.finishBlock();
        globalConstructor.getReturnBlock().appendInstruction(new IRReturnInstruction(module.getIRType("void"), null));
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
                    currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, currentFunction.getReturnBlock(), null, module));
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
        node.getStatements().forEach(statement -> statement.accept(this));
        // void function or main could have no return statement
        if (!((FunctionScope) currentScope).hasReturnStatement()) {
            currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, currentFunction.getReturnBlock(), null, module));
            currentFunction.getReturnBlock().appendInstruction(new IRReturnInstruction(returnIRType, returnIRType.getDefaultValue()));
        } else {
            IRRegister returnValue = new IRRegister(returnIRType);
            currentFunction.getReturnBlock().appendInstruction(new IRLoadInstruction(returnIRType, returnValue, returnValuePtr));
            currentFunction.getReturnBlock().appendInstruction(new IRReturnInstruction(returnIRType, returnValue));
        }
        currentScope = currentScope.getParentScope();
        currentBasicBlock.finishBlock();
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
            currentBasicBlock.appendInstruction(new IRStoreInstruction(parameterType, targetRegister, new IRRegister(new IRPointerType(parameterType), srcRegisterId)));
        }
    }

    @Override
    public void visit(BlockStatementNode node) {

    }

    @Override
    public void visit(IfStatementNode node) {

    }

    @Override
    public void visit(ForStatementNode node) {

    }

    @Override
    public void visit(WhileStatementNode node) {

    }

    @Override
    public void visit(ReturnStatementNode node) {
        node.getReturnValue().accept(this);
        IROperand returnValueRegister = node.getReturnValue().getIRResultValue();
        // return value should be store in a specific IRRegister, which will be created at the
        // entry block, i.e., the first time visit FunctionDefineNode, and escape block return that register.
        currentBasicBlock.appendInstruction(new IRStoreInstruction(currentFunction.getReturnType(), currentScope.getReturnValuePtr(), returnValueRegister));
        // return statement should create an escape (i.e., branch) instruction to returnBlock.
        currentBasicBlock.setEscapeInstruction(new IRBrInstruction(null, currentFunction.getReturnBlock(), null, module));
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
            if (function == null) log.Errorf("[IRBuilder::visit(FunctionCallExpressionNode] IRFunction %s not found", node.getFunctionName());
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
        // left value: variable, array addressing, assign, member access, prefix crement
        // self-crement need update register in VariableEntity since value has changed
        node.getLhs().accept(this);

    }

    @Override
    public void visit(UnaryExpressionNode node) {

    }

    @Override
    public void visit(BinaryExpressionNode node) {

    }

    @Override
    public void visit(AssignExpressionNode node) {

    }

    @Override
    public void visit(ThisPrimaryNode node) {

    }

    @Override
    public void visit(IdentifierPrimaryNode node) {
        if (!node.isVariable()) throw new IRError("visit FunctionIdentifier in IRBuilder");
        VariableEntity entity = currentScope.getVariableEntityRecursively(node.getIdentifier());
        IRTypeSystem loadType = entity.getVariableType().toIRType(module);
        IRRegister loadTarget = new IRRegister(new IRPointerType(loadType));
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

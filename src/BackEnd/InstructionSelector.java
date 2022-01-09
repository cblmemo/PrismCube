package BackEnd;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.ASMModule;
import ASM.Instruction.*;
import ASM.Operand.*;
import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.IRFunction;
import IR.IRGlobalDefine;
import IR.IRModule;
import IR.Instruction.*;
import IR.Operand.*;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRStructureType;
import Memory.Memory;
import Utility.error.ASMError;

import java.util.HashMap;

public class InstructionSelector implements IRVisitor {
    private ASMFunction currentFunction;
    private ASMBasicBlock currentBasicBlock;

    private HashMap<String, ASMFunction> builtinFunctions;
    private HashMap<String, ASMFunction> functions;

    // llvm Register to Virtual Register
    private final HashMap<IRRegister, ASMVirtualRegister> lr2vr = new HashMap<>();

    static boolean select = false;

    public static void enable() {
        select = true;
    }

    public static void disable() {
        select = false;
    }

    public void select(Memory memory) {
        if (select) {
            ASMModule asmModule = memory.getAsmModule();
            functions = asmModule.getFunctions();
            builtinFunctions = asmModule.getBuiltinFunctions();
            memory.getIRModule().accept(this);
        }
    }

    private void appendInst(ASMInstruction inst) {
        currentBasicBlock.appendInstruction(inst);
    }

    private void appendPseudoInst(ASMPseudoInstruction.InstType type, ASMOperand... operands) {
        ASMPseudoInstruction pseudoInst = new ASMPseudoInstruction(type);
        for (ASMOperand operand : operands) pseudoInst.addOperand(operand);
        appendInst(pseudoInst);
    }

    private void appendArithmeticInst(ASMArithmeticInstruction.InstType type, ASMOperand... operands) {
        ASMArithmeticInstruction arithmeticInstruction = new ASMArithmeticInstruction(type);
        for (ASMOperand operand : operands) arithmeticInstruction.addOperand(operand);
        appendInst(arithmeticInstruction);
    }

    private ASMVirtualRegister toVirtualRegister(IROperand operand) {
        if (operand instanceof IRRegister) {
            if (operand instanceof IRGlobalVariableRegister) {
                // todo
            } else {
                if (!lr2vr.containsKey((IRRegister) operand)) lr2vr.put((IRRegister) operand, new ASMVirtualRegister(((IRRegister) operand).getName()));
                return lr2vr.get((IRRegister) operand);
            }
        }
        if (operand instanceof IRConstString) {
            // todo
        }
        assert operand instanceof IRConstNumber;
        int imm = ((IRConstNumber) operand).getIntValue();
        ASMVirtualRegister reg = new ASMVirtualRegister("const");
        appendPseudoInst(ASMPseudoInstruction.InstType.li, reg, new ASMImmediate(imm));
        return reg;
    }

    private ASMOperand toOperand(IROperand operand) {
        if (operand instanceof IRRegister) return toVirtualRegister(operand);
        assert operand instanceof IRConstNumber;
        return new ASMImmediate(((IRConstNumber) operand).getIntValue());
    }

    private void parseArith(ASMArithmeticInstruction.InstType type, ASMVirtualRegister rd, IROperand rs1, IROperand rs2, boolean inverse) {
        ASMArithmeticInstruction.InstType newType;
        ASMOperand rs1V, rs2V;
        if (type.swappable() && rs1 instanceof IRConstNumber && !(rs2 instanceof IRConstNumber)) {
            newType = type.toImmediateType();
            rs1V = toVirtualRegister(rs2);
            rs2V = toOperand(rs1);
        } else {
            rs1V = toVirtualRegister(rs1);
            rs2V = type.haveImmediateType() ? toOperand(rs2) : toVirtualRegister(rs2);
            newType = type.haveImmediateType() && rs2V instanceof ASMImmediate ? type.toImmediateType() : type;
        }
        if (inverse) {
            ASMVirtualRegister temp = new ASMVirtualRegister("before_inverse");
            appendArithmeticInst(newType, temp, rs1V, rs2V);
            appendArithmeticInst(ASMArithmeticInstruction.InstType.xori, rd, temp, new ASMImmediate(1));
        } else appendArithmeticInst(newType, rd, rs1V, rs2V);
    }

    @Override
    public void visit(IRModule module) {
        // todo const string and global variable
        module.getFunctions().forEach((name, function) -> functions.put(name, new ASMFunction(function)));
        module.getBuiltinFunctions().forEach((name, function) -> {
            if (function.hasCalled()) builtinFunctions.put(name, new ASMFunction(function.getFunctionName()));
        });
        module.getFunctions().values().forEach(function -> function.accept(this));
    }

    @Override
    public void visit(IRGlobalDefine define) {
        // todo
    }

    @Override
    public void visit(IRFunction function) {
        currentFunction = functions.get(function.getFunctionName());
        currentBasicBlock = currentFunction.getEntryBlock();
        appendInst(null); // will be replaced by "sp -= frame size"
        // initialize stack frame
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst instanceof IRCallInstruction) currentFunction.getStackFrame().updateMaxArgumentNumber(((IRCallInstruction) inst).getArgumentNumber());
        }));
        function.getEntryBlock().getAllocas().forEach(alloca -> alloca.accept(this));
        // get arguments
        for (int i = 0; i < Integer.min(function.getParameterNumber(), 8); i++)
            appendPseudoInst(ASMPseudoInstruction.InstType.mv, toVirtualRegister(function.getParameters().get(i)), ASMPhysicalRegister.getArgumentRegister(i));
        for (int i = 8; i < function.getParameterNumber(); i++) {
            ASMAddress argumentAddress = new ASMAddress(ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp), new ASMImmediate(4 * (i - 8)));
            argumentAddress.markAsNeedAddFrameSize(currentFunction.getStackFrame());
            appendInst(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.lw, toVirtualRegister(function.getParameters().get(i)), argumentAddress));
        }
        // callee save register backup
        ASMPhysicalRegister.getCalleeSaveRegisters().forEach(reg -> {
            ASMVirtualRegister calleeSave = new ASMVirtualRegister(reg + "_duplicate");
            appendPseudoInst(ASMPseudoInstruction.InstType.mv, calleeSave, reg);
            currentFunction.addCalleeSave(reg, calleeSave);
        });
        function.getBlocks().forEach(block -> block.accept(this));
    }

    @Override
    public void visit(IRBasicBlock block) {
        currentBasicBlock = currentFunction.getASMBasicBlock(block);
        block.getInstructions().forEach(inst -> inst.accept(this));
    }

    @Override
    public void visit(IRConstString string) {
        // todo
    }

    @Override
    public void visit(IRStructureType type) {

    }

    @Override
    public void visit(IRBrInstruction inst) {
        // br l1         -> j l1
        //
        // br %0 l1 l2   -> beqz l2
        //                  j l1
        if (inst.isBranch()) appendPseudoInst(ASMPseudoInstruction.InstType.beqz, toVirtualRegister(inst.getCondition()), currentFunction.getBasicBlockLabel(inst.getElseBlock()));
        appendPseudoInst(ASMPseudoInstruction.InstType.j, currentFunction.getBasicBlockLabel(inst.getThenBlock()));
    }

    @Override
    public void visit(IRCallInstruction inst) {
        // caller save register backup
        HashMap<ASMPhysicalRegister, ASMVirtualRegister> callerSaves = new HashMap<>();
        ASMPhysicalRegister.getCallerSaveRegisters().forEach(reg -> {
            ASMVirtualRegister callerSave = new ASMVirtualRegister(reg + "_duplicate");
            appendPseudoInst(ASMPseudoInstruction.InstType.mv, callerSave, reg);
            callerSaves.put(reg, callerSave);
        });
        // put arguments in register, if more than 8 put in stack
        for (int i = 0; i < Integer.min(inst.getArgumentNumber(), 8); i++)
            appendPseudoInst(ASMPseudoInstruction.InstType.mv, ASMPhysicalRegister.getArgumentRegister(i), toVirtualRegister(inst.getArgumentValues().get(i)));
        for (int i = 8; i < inst.getArgumentNumber(); i++) {
            ASMAddress argumentAddress = new ASMAddress(ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp), new ASMImmediate(4 * (i - 8)));
            appendInst(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.sw, toVirtualRegister(inst.getArgumentValues().get(i)), argumentAddress));
        }
        appendPseudoInst(ASMPseudoInstruction.InstType.call, functions.get(inst.getCallFunction().getFunctionName()).getLabel());
        if (inst.haveReturnValue()) appendPseudoInst(ASMPseudoInstruction.InstType.mv, toVirtualRegister(inst.getResultRegister()), ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.a0));
        // retrieve callee save register
        ASMPhysicalRegister.getCallerSaveRegisters().forEach(reg -> appendPseudoInst(ASMPseudoInstruction.InstType.mv, reg, callerSaves.get(reg)));
    }

    @Override
    public void visit(IRLoadInstruction inst) {
        ASMVirtualRegister loadTarget = toVirtualRegister(inst.getLoadTarget());
        ASMAddress loadSource = new ASMAddress(toVirtualRegister(inst.getLoadValue()), null);
        ASMMemoryInstruction.InstType loadType = inst.getLoadType().sizeof() == 1 ? ASMMemoryInstruction.InstType.lb : ASMMemoryInstruction.InstType.lw;
        appendInst(new ASMMemoryInstruction(loadType, loadTarget, loadSource));
    }

    @Override
    public void visit(IRReturnInstruction inst) {
        // retrieve callee save register
        ASMPhysicalRegister.getCalleeSaveRegisters().forEach(reg -> appendPseudoInst(ASMPseudoInstruction.InstType.mv, reg, currentFunction.getCalleeSave(reg)));
        // put return value in a0
        if (inst.hasReturnValue()) appendPseudoInst(ASMPseudoInstruction.InstType.mv, ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.a0), toVirtualRegister(inst.getReturnValue()));
        appendInst(null); // will be replaced by "sp += frame size"
        appendPseudoInst(ASMPseudoInstruction.InstType.ret);
    }

    @Override
    public void visit(IRAllocaInstruction inst) {
        ASMImmediate offset = new ASMImmediate(currentFunction.getStackFrame().getAllocaRegisterOffset(inst.getAllocaTarget()));
        appendArithmeticInst(ASMArithmeticInstruction.InstType.add, toVirtualRegister(inst.getAllocaTarget()), ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp), offset);
    }

    @Override
    public void visit(IRStoreInstruction inst) {
        ASMVirtualRegister storeValue = toVirtualRegister(inst.getStoreValue());
        ASMAddress storeTarget = new ASMAddress(toVirtualRegister(inst.getStoreTarget()), null);
        ASMMemoryInstruction.InstType storeType = inst.getStoreType().sizeof() == 1 ? ASMMemoryInstruction.InstType.sb : ASMMemoryInstruction.InstType.sw;
        appendInst(new ASMMemoryInstruction(storeType, storeValue, storeTarget));
    }

    @Override
    public void visit(IRBinaryInstruction inst) {
        ASMVirtualRegister result = toVirtualRegister(inst.getResultRegister());
        IROperand lhs = inst.getLhs();
        IROperand rhs = inst.getRhs();
        switch (inst.getOp()) {
            case "add" -> parseArith(ASMArithmeticInstruction.InstType.add, result, lhs, rhs, false);
            case "sub nsw" -> parseArith(ASMArithmeticInstruction.InstType.sub, result, lhs, rhs, false);
            case "mul" -> parseArith(ASMArithmeticInstruction.InstType.mul, result, lhs, rhs, false);
            case "sdiv" -> parseArith(ASMArithmeticInstruction.InstType.div, result, lhs, rhs, false);
            case "srem" -> parseArith(ASMArithmeticInstruction.InstType.rem, result, lhs, rhs, false);
            case "shl nsw" -> parseArith(ASMArithmeticInstruction.InstType.sll, result, lhs, rhs, false);
            case "ashr" -> parseArith(ASMArithmeticInstruction.InstType.sra, result, lhs, rhs, false);
            case "and" -> parseArith(ASMArithmeticInstruction.InstType.and, result, lhs, rhs, false);
            case "xor" -> parseArith(ASMArithmeticInstruction.InstType.xor, result, lhs, rhs, false);
            case "or" -> parseArith(ASMArithmeticInstruction.InstType.or, result, lhs, rhs, false);
            default -> throw new ASMError("unknown binary type");
        }
    }

    @Override
    public void visit(IRIcmpInstruction inst) {
        ASMVirtualRegister result = toVirtualRegister(inst.getResultRegister());
        IROperand lhs = inst.getLhs();
        IROperand rhs = inst.getRhs();
        switch (inst.getOp()) {
            case "slt" -> parseArith(ASMArithmeticInstruction.InstType.slt, result, lhs, rhs, false);
            case "sle" -> parseArith(ASMArithmeticInstruction.InstType.slt, result, rhs, lhs, true);
            case "sgt" -> parseArith(ASMArithmeticInstruction.InstType.slt, result, rhs, lhs, false);
            case "sge" -> parseArith(ASMArithmeticInstruction.InstType.slt, result, lhs, rhs, true);
            case "eq" -> {
                ASMVirtualRegister temp = new ASMVirtualRegister("temp");
                parseArith(ASMArithmeticInstruction.InstType.sub, temp, lhs, rhs, false);
                appendPseudoInst(ASMPseudoInstruction.InstType.seqz, result, temp);
            }
            case "ne" -> {
                ASMVirtualRegister temp = new ASMVirtualRegister("temp");
                parseArith(ASMArithmeticInstruction.InstType.sub, temp, lhs, rhs, false);
                appendPseudoInst(ASMPseudoInstruction.InstType.snez, result, temp);
            }
            default -> throw new ASMError("unknown icmp type");
        }
    }

    @Override
    public void visit(IRTruncInstruction inst) {
        // directly move since all trunc instruction are used to deal with conversion between i1 and i8
        appendPseudoInst(ASMPseudoInstruction.InstType.mv, toVirtualRegister(inst.getTruncResultRegister()), toVirtualRegister(inst.getTruncTarget()));
    }

    @Override
    public void visit(IRZextInstruction inst) {
        // directly move since all zext instruction are used to deal with conversion between i1 and i8
        appendPseudoInst(ASMPseudoInstruction.InstType.mv, toVirtualRegister(inst.getZextResultRegister()), toVirtualRegister(inst.getZextTarget()));
    }

    @Override
    public void visit(IRGetelementptrInstruction inst) {
        switch (inst.getIndices().size()) {
            case 1 -> {
                assert inst.getPtrValue() instanceof IRRegister && !(inst.getPtrValue() instanceof IRGlobalVariableRegister);
                ASMVirtualRegister offsetRegister = new ASMVirtualRegister("gep_offset");
                parseArith(ASMArithmeticInstruction.InstType.mul, offsetRegister, inst.getIndices().get(0), new IRConstInt(null, inst.getElementType().sizeof()), false);
                appendArithmeticInst(ASMArithmeticInstruction.InstType.add, toVirtualRegister(inst.getResultRegister()), toVirtualRegister(inst.getPtrValue()), offsetRegister);
            }
            case 2 -> { // member access
                assert inst.getPtrValue() instanceof IRRegister;
                assert inst.getElementType() instanceof IRPointerType;
                assert ((IRPointerType) inst.getElementType()).getBaseType() instanceof IRStructureType;
                assert inst.getIndices().get(0) instanceof IRConstInt;
                assert ((IRConstInt) inst.getIndices().get(0)).getIntValue() == 0;
                assert inst.getIndices().get(1) instanceof IRConstInt;
                int index = ((IRConstInt) inst.getIndices().get(1)).getIntValue();
                IRStructureType classType = (IRStructureType) ((IRPointerType) inst.getElementType()).getBaseType();
                int offset = classType.getMemberOffset(index);
                parseArith(ASMArithmeticInstruction.InstType.add, toVirtualRegister(inst.getResultRegister()), inst.getPtrValue(), new IRConstInt(null, offset), false);
            }
            default -> throw new ASMError("wrong gep indices num");
        }
    }

    @Override
    public void visit(IRBitcastInstruction inst) {
        // straightly move since type of ptr doesn't matter in asm
        appendPseudoInst(ASMPseudoInstruction.InstType.mv, toVirtualRegister(inst.getResultRegister()), toVirtualRegister(inst.getPtrValue()));
    }
}

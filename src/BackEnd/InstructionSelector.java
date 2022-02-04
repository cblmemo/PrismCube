package BackEnd;

import ASM.ASMBasicBlock;
import ASM.Operand.ASMConstString;
import ASM.ASMFunction;
import ASM.ASMModule;
import ASM.Instruction.*;
import ASM.Operand.*;
import ASM.Operand.GlobalSymbol.ASMGlobalBoolean;
import ASM.Operand.GlobalSymbol.ASMGlobalInteger;
import ASM.Operand.GlobalSymbol.ASMGlobalString;
import ASM.Operand.GlobalSymbol.ASMGlobalSymbol;
import FrontEnd.IRVisitor;
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
import Utility.error.ASMError;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class select appropriate instruction in
 * rv32i instructions (and pseudo instructions).
 * For all pseudo instruction used,
 *
 * @author rainy memory
 * @version 1.0.0
 * @see ASMPseudoInstruction.InstType
 */

public class InstructionSelector implements IRVisitor {
    private ASMModule asmModule;
    private ASMFunction currentFunction;
    private ASMBasicBlock currentBasicBlock;

    private LinkedHashMap<String, ASMFunction> builtinFunctions;
    private LinkedHashMap<String, ASMFunction> functions;

    // llvm Register to ASM Register
    private final LinkedHashMap<IRRegister, ASMRegister> lr2r = new LinkedHashMap<>();

    private static boolean select = false;

    public static void enable() {
        select = true;
    }

    public static void disable() {
        select = false;
    }

    public void select(Memory memory) {
        if (select) {
            asmModule = memory.getAsmModule();
            functions = asmModule.getFunctions();
            builtinFunctions = asmModule.getBuiltinFunctions();
            memory.getIRModule().accept(this);
            new ASMEmitter().emitVirtual(memory);
        }
    }

    private void appendInst(ASMInstruction inst) {
        currentBasicBlock.appendInstruction(inst);
    }

    private void appendPseudoInst(ASMPseudoInstruction.InstType type, ASMOperand... operands) {
        ASMPseudoInstruction pseudoInst = type.isMove() ? new ASMMoveInstruction(type) : new ASMPseudoInstruction(type);
        for (ASMOperand operand : operands) pseudoInst.addOperand(operand);
        appendInst(pseudoInst);
    }

    private void appendArithmeticInst(ASMArithmeticInstruction.InstType type, ASMOperand... operands) {
        ASMArithmeticInstruction arithmeticInstruction = new ASMArithmeticInstruction(type);
        for (ASMOperand operand : operands) arithmeticInstruction.addOperand(operand);
        appendInst(arithmeticInstruction);
    }

    private String getIRConstStringName(IRConstString string) {
        // @see ASMGlobalString.getValue
        return ".L.str." + string.getId();
    }

    private ASMLabel getFunctionLabel(String functionName) {
        if (functions.containsKey(functionName)) return functions.get(functionName).getLabel();
        assert builtinFunctions.containsKey(functionName);
        return builtinFunctions.get(functionName).getLabel();
    }

    private ASMRegister toRegister(IROperand operand) {
        if (operand instanceof IRRegister) {
            if (!lr2r.containsKey((IRRegister) operand)) lr2r.put((IRRegister) operand, new ASMVirtualRegister(((IRRegister) operand).getName()));
            return lr2r.get((IRRegister) operand);
        }
        if (operand instanceof IRConstString) {
            ASMVirtualRegister string = new ASMVirtualRegister("address");
            appendPseudoInst(ASMPseudoInstruction.InstType.la, string, asmModule.getConstString(getIRConstStringName((IRConstString) operand)));
            return string;
        }
        assert operand instanceof IRConstNumber;
        int imm = ((IRConstNumber) operand).getIntValue();
        if (imm == 0) return ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.zero);
        ASMVirtualRegister reg = new ASMVirtualRegister("const");
        appendPseudoInst(ASMPseudoInstruction.InstType.li, reg, new ASMImmediate(imm));
        return reg;
    }

    private boolean isValidImmediate(int imm) {
        return -2048 <= imm && imm <= 2047;
    }

    private ASMOperand toOperand(IROperand operand) {
        if (operand instanceof IRRegister) return toRegister(operand);
        assert operand instanceof IRConstNumber;
        int value = ((IRConstNumber) operand).getIntValue();
        if (value == 0) return ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.zero);
        if (isValidImmediate(value)) return new ASMImmediate(value);
        return toRegister(operand);
    }

    private void parseArith(ASMArithmeticInstruction.InstType type, ASMRegister rd, IROperand rs1, IROperand rs2, boolean inverse) {
        ASMArithmeticInstruction.InstType newType;
        ASMOperand rs1V, rs2V;
        if (type.swappable() && rs1 instanceof IRConstNumber && !(rs2 instanceof IRConstNumber)) {
            newType = type.toImmediateType();
            rs1V = toRegister(rs2);
            rs2V = toOperand(rs1);
        } else {
            rs1V = toRegister(rs1);
            rs2V = type.haveImmediateType() ? toOperand(rs2) : toRegister(rs2);
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
        // visit string first since global variable might use it
        module.getStrings().values().forEach(string -> string.accept(this));
        module.getGlobalDefines().values().forEach(global -> global.accept(this));
        module.getFunctions().forEach((name, function) -> functions.put(name, new ASMFunction(function)));
        module.getBuiltinFunctions().forEach((name, function) -> {
            if (function.hasCalled()) builtinFunctions.put(name, new ASMFunction(function.getFunctionName()));
        });
        module.getFunctions().values().forEach(function -> function.accept(this));
    }

    @Override
    public void visit(IRGlobalDefine define) {
        String name = define.getVariableName();
        IRTypeSystem type = define.getVariableType();
        ASMGlobalSymbol symbol;
        // pointer is int in assembly
        if (type.isInt() || type.isPointer()) symbol = new ASMGlobalInteger(name, define);
        else if (type.isBool() || type.isChar()) symbol = new ASMGlobalBoolean(name, define);
        else {
            assert type.isString() : type;
            symbol = new ASMGlobalString(name, define);
        }
        asmModule.addGlobal(name, symbol);
    }

    @Override
    public void visit(IRConstString string) {
        String name = getIRConstStringName(string);
        asmModule.addConstString(name, new ASMConstString(string, getIRConstStringName(string)));
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
        if (!RegisterAllocator.naive())
            // callee save register backup
            ASMPhysicalRegister.getCalleeSaveRegisters().forEach(reg -> {
                ASMVirtualRegister calleeSave = new ASMVirtualRegister(reg + "_backup");
                appendPseudoInst(ASMPseudoInstruction.InstType.mv, calleeSave, reg);
                currentFunction.addCalleeSave(reg, calleeSave);
            });
        // get arguments
        for (int i = 0; i < Integer.min(function.getParameterNumber(), 8); i++) {
            // directly use a0 - a7 as register of parameter
            lr2r.put(function.getParameters().get(i), ASMPhysicalRegister.getArgumentRegister(i));
        }
        for (int i = 8; i < function.getParameterNumber(); i++) {
            ASMAddress argumentAddress = new ASMAddress(ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp), new ASMImmediate(4 * (i - 8)));
            argumentAddress.markAsNeedAddFrameSize(currentFunction.getStackFrame());
            appendInst(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.lw, toRegister(function.getParameters().get(i)), argumentAddress));
        }
        function.getBlocks().forEach(block -> block.accept(this));
    }

    @Override
    public void visit(IRBasicBlock block) {
        currentBasicBlock = currentFunction.getASMBasicBlock(block);
        block.getInstructions().forEach(inst -> inst.accept(this));
    }

    @Override
    public void visit(IRStructureType type) {

    }

    @Override
    public void visit(IRBrInstruction inst) {
        // br %0 l1 l2   -> beqz %0 l2
        //                  j l1
        appendPseudoInst(ASMPseudoInstruction.InstType.beqz, toRegister(inst.getCondition()), currentFunction.getBasicBlockLabel(inst.getElseBlock()));
        appendPseudoInst(ASMPseudoInstruction.InstType.j, currentFunction.getBasicBlockLabel(inst.getThenBlock()));
    }

    @Override
    public void visit(IRJumpInstruction inst) {
        // br l1         -> j l1
        appendPseudoInst(ASMPseudoInstruction.InstType.j, currentFunction.getBasicBlockLabel(inst.getTargetBlock()));
    }

    @Override
    public void visit(IRCallInstruction inst) {
        LinkedHashMap<ASMPhysicalRegister, ASMVirtualRegister> callerSaves = new LinkedHashMap<>();
        // naive allocator doesn't need to back up caller save except for ra since it store all value on stack
        // graph coloring also doesn't need to back up since all caller save registers is treated as defs of call instruction
        if (RegisterAllocator.naive()) {
            ASMPhysicalRegister ra = ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.ra);
            ASMVirtualRegister raBackup = new ASMVirtualRegister("ra_backup");
            appendPseudoInst(ASMPseudoInstruction.InstType.mv, raBackup, ra);
            callerSaves.put(ra, raBackup);
        }
        // put arguments in register, if more than 8 put in stack
        for (int i = 0; i < Integer.min(inst.getArgumentNumber(), 8); i++)
            appendPseudoInst(ASMPseudoInstruction.InstType.mv, ASMPhysicalRegister.getArgumentRegister(i), toRegister(inst.getArgumentValues().get(i)));
        for (int i = 8; i < inst.getArgumentNumber(); i++) {
            ASMAddress argumentAddress = new ASMAddress(ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp), new ASMImmediate(4 * (i - 8)));
            appendInst(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.sw, toRegister(inst.getArgumentValues().get(i)), argumentAddress));
        }
        ASMLabel functionLabel = getFunctionLabel(inst.getCallFunction().getFunctionName());
        appendPseudoInst(ASMPseudoInstruction.InstType.call, functionLabel);
        if (inst.haveReturnValue()) appendPseudoInst(ASMPseudoInstruction.InstType.mv, toRegister(inst.getResultRegister()), ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.a0));// retrieve caller save register
        if (RegisterAllocator.naive()) callerSaves.forEach((reg, backup) -> appendPseudoInst(ASMPseudoInstruction.InstType.mv, reg, backup));
    }

    @Override
    public void visit(IRLoadInstruction inst) {
        ASMRegister loadTarget = toRegister(inst.getLoadTarget());
        ASMAddress loadSource;
        if (inst.getLoadValue() instanceof IRGlobalVariableRegister) {
            ASMVirtualRegister address = new ASMVirtualRegister("global_address");
            appendPseudoInst(ASMPseudoInstruction.InstType.la, address, asmModule.getGlobal(((IRGlobalVariableRegister) inst.getLoadValue()).getGlobalVariableName()));
            loadSource = new ASMAddress(address, null);
        } else {
            if (currentFunction.getStackFrame().isAllocaRegister((IRRegister) inst.getLoadValue())) {
                int offset = currentFunction.getStackFrame().getAllocaRegisterOffset((IRRegister) inst.getLoadValue());
                loadSource = new ASMAddress(ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp), new ASMImmediate(offset));
            } else loadSource = new ASMAddress(toRegister(inst.getLoadValue()), null);
        }
        ASMMemoryInstruction.InstType loadType = inst.getLoadType().sizeof() == 1 ? ASMMemoryInstruction.InstType.lb : ASMMemoryInstruction.InstType.lw;
        appendInst(new ASMMemoryInstruction(loadType, loadTarget, loadSource));
    }

    @Override
    public void visit(IRReturnInstruction inst) {
        if (!RegisterAllocator.naive())
            // retrieve callee save register
            currentFunction.getCalleeSaves().forEach((reg, backup) -> appendPseudoInst(ASMPseudoInstruction.InstType.mv, reg, backup));
        // put return value in a0
        if (inst.hasReturnValue()) appendPseudoInst(ASMPseudoInstruction.InstType.mv, ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.a0), toRegister(inst.getReturnValue()));
        appendInst(null); // will be replaced by "sp += frame size"
        appendPseudoInst(ASMPseudoInstruction.InstType.ret);
    }

    @Override
    public void visit(IRAllocaInstruction inst) {
        // do nothing since already initialize all alloca instruction in constructor of ASMFunction
    }

    @Override
    public void visit(IRStoreInstruction inst) {
        ASMRegister storeValue = toRegister(inst.getStoreValue());
        ASMAddress storeTarget;
        if (inst.getStoreTarget() instanceof IRGlobalVariableRegister) {
            ASMVirtualRegister address = new ASMVirtualRegister("global_address");
            appendPseudoInst(ASMPseudoInstruction.InstType.la, address, asmModule.getGlobal(((IRGlobalVariableRegister) inst.getStoreTarget()).getGlobalVariableName()));
            storeTarget = new ASMAddress(address, null);
        } else {
            if (currentFunction.getStackFrame().isAllocaRegister((IRRegister) inst.getStoreTarget())) {
                int offset = currentFunction.getStackFrame().getAllocaRegisterOffset((IRRegister) inst.getStoreTarget());
                storeTarget = new ASMAddress(ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp), new ASMImmediate(offset));
            } else storeTarget = new ASMAddress(toRegister(inst.getStoreTarget()), null);
        }
        ASMMemoryInstruction.InstType storeType = inst.getStoreType().sizeof() == 1 ? ASMMemoryInstruction.InstType.sb : ASMMemoryInstruction.InstType.sw;
        appendInst(new ASMMemoryInstruction(storeType, storeValue, storeTarget));
    }

    private static final LinkedHashMap<String, ASMArithmeticInstruction.InstType> ir2asm = new LinkedHashMap<>(Map.of(
            "add", ASMArithmeticInstruction.InstType.add,
            "sub nsw", ASMArithmeticInstruction.InstType.sub,
            "mul", ASMArithmeticInstruction.InstType.mul,
            "sdiv", ASMArithmeticInstruction.InstType.div,
            "srem", ASMArithmeticInstruction.InstType.rem,
            "shl nsw", ASMArithmeticInstruction.InstType.sll,
            "ashr", ASMArithmeticInstruction.InstType.sra,
            "and", ASMArithmeticInstruction.InstType.and,
            "xor", ASMArithmeticInstruction.InstType.xor,
            "or", ASMArithmeticInstruction.InstType.or
    ));

    @Override
    public void visit(IRBinaryInstruction inst) {
        ASMRegister result = toRegister(inst.getResultRegister());
        parseArith(ir2asm.get(inst.getOp()), result, inst.getLhs(), inst.getRhs(), false);
    }

    @Override
    public void visit(IRIcmpInstruction inst) {
        ASMRegister result = toRegister(inst.getResultRegister());
        IROperand lhs = inst.getLhs(), rhs = inst.getRhs();
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
        appendPseudoInst(ASMPseudoInstruction.InstType.mv, toRegister(inst.getTruncResultRegister()), toRegister(inst.getTruncTarget()));
    }

    @Override
    public void visit(IRZextInstruction inst) {
        // directly move since all zext instruction are used to deal with conversion between i1 and i8
        appendPseudoInst(ASMPseudoInstruction.InstType.mv, toRegister(inst.getZextResultRegister()), toRegister(inst.getZextTarget()));
    }

    @Override
    public void visit(IRGetelementptrInstruction inst) {
        ASMRegister result = toRegister(inst.getResultRegister());
        switch (inst.getIndices().size()) {
            case 1 -> {
                assert inst.getPtrValue() instanceof IRRegister;
                ASMVirtualRegister offsetRegister = new ASMVirtualRegister("gep_offset");
                parseArith(ASMArithmeticInstruction.InstType.mul, offsetRegister, inst.getIndices().get(0), new IRConstInt(null, inst.getElementType().sizeof()), false);
                appendArithmeticInst(ASMArithmeticInstruction.InstType.add, result, toRegister(inst.getPtrValue()), offsetRegister);
            }
            case 2 -> { // member access
                assert inst.getPtrValue() instanceof IRRegister;
                assert inst.getElementType() instanceof IRStructureType;
                assert inst.getIndices().get(0) instanceof IRConstInt;
                assert ((IRConstInt) inst.getIndices().get(0)).getIntValue() == 0;
                assert inst.getIndices().get(1) instanceof IRConstInt;
                int index = ((IRConstInt) inst.getIndices().get(1)).getIntValue();
                IRStructureType classType = (IRStructureType) inst.getElementType();
                int offset = classType.getMemberOffset(index);
                parseArith(ASMArithmeticInstruction.InstType.add, result, inst.getPtrValue(), new IRConstInt(null, offset), false);
            }
            default -> throw new ASMError("invalid gep indices num");
        }
    }

    @Override
    public void visit(IRBitcastInstruction inst) {
        // directly move since type of ptr doesn't matter in asm
        appendPseudoInst(ASMPseudoInstruction.InstType.mv, toRegister(inst.getResultRegister()), toRegister(inst.getPtrValue()));
    }
}

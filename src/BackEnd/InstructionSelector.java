package BackEnd;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.ASMModule;
import ASM.Instruction.ASMInstruction;
import ASM.Instruction.ASMLoadInstruction;
import ASM.Instruction.ASMPseudoInstruction;
import ASM.Instruction.ASMStoreInstruction;
import ASM.Operand.ASMImmediate;
import ASM.Operand.ASMVirtualRegister;
import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.IRFunction;
import IR.IRGlobalDefine;
import IR.IRModule;
import IR.Instruction.*;
import IR.Operand.*;
import IR.TypeSystem.IRStructureType;
import Memory.Memory;

import java.util.HashMap;

public class InstructionSelector implements IRVisitor {
    private ASMModule asmModule;
    private ASMFunction currentFunction;
    private ASMBasicBlock currentBasicBlock;

    private final HashMap<String, ASMFunction> functions = new HashMap<>();

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
            asmModule = memory.getAsmModule();
            memory.getIRModule().accept(this);
        }
    }

    private void appendInst(ASMInstruction inst) {
        currentBasicBlock.appendInstruction(inst);
    }

    private ASMVirtualRegister toVirtualRegister(IROperand operand) {
        if (operand instanceof IRRegister) {
            if (!lr2vr.containsKey((IRRegister) operand)) lr2vr.put((IRRegister) operand, new ASMVirtualRegister(((IRRegister) operand).getName()));
            return lr2vr.get((IRRegister) operand);
        }
        int imm;
        if (operand instanceof IRConstInt) imm = ((IRConstInt) operand).getValue();
        else if (operand instanceof IRConstBool) imm = ((IRConstBool) operand).getValue() ? 1 : 0;
        else {
            assert operand instanceof IRNull;
            imm = 0;
        }
        ASMVirtualRegister reg = new ASMVirtualRegister("to_vr");
        ASMPseudoInstruction liInst = new ASMPseudoInstruction(ASMPseudoInstruction.InstType.li);
        liInst.addOperand(reg).addOperand(new ASMImmediate(imm));
        appendInst(liInst);
        return reg;
    }

    @Override
    public void visit(IRModule module) {
        module.getFunctions().keySet().forEach(name -> functions.put(name, new ASMFunction(name)));
        module.getFunctions().values().forEach(function -> function.accept(this));
    }

    @Override
    public void visit(IRGlobalDefine define) {

    }

    @Override
    public void visit(IRFunction function) {
        currentFunction = functions.get(function.getFunctionName());
        currentBasicBlock = currentFunction.getEntryBlock();
        function.getBlocks().forEach(block -> block.accept(this));
    }

    @Override
    public void visit(IRBasicBlock block) {
        block.getInstructions().forEach(inst -> inst.accept(this));
    }

    @Override
    public void visit(IRConstString string) {

    }

    @Override
    public void visit(IRStructureType type) {

    }

    @Override
    public void visit(IRBrInstruction inst) {

    }

    @Override
    public void visit(IRCallInstruction inst) {

    }

    @Override
    public void visit(IRLoadInstruction inst) {
        ASMVirtualRegister loadTarget = toVirtualRegister(inst.getLoadTarget());
        ASMVirtualRegister loadSource = toVirtualRegister(inst.getLoadValue());
        ASMLoadInstruction.InstType loadType = inst.getLoadType().sizeof() == 1 ? ASMLoadInstruction.InstType.lb : ASMLoadInstruction.InstType.lw;
        appendInst(new ASMLoadInstruction(loadType, loadTarget, loadSource, null));
    }

    @Override
    public void visit(IRReturnInstruction inst) {

    }

    @Override
    public void visit(IRAllocaInstruction inst) {

    }

    @Override
    public void visit(IRStoreInstruction inst) {
        ASMVirtualRegister storeValue = toVirtualRegister(inst.getStoreValue());
        ASMVirtualRegister storeTarget = toVirtualRegister(inst.getStoreTarget());
        ASMStoreInstruction.InstType storeType = inst.getStoreType().sizeof() == 1 ? ASMStoreInstruction.InstType.sb : ASMStoreInstruction.InstType.sw;
        appendInst(new ASMStoreInstruction(storeType, storeValue, storeTarget, null));
    }

    @Override
    public void visit(IRBinaryInstruction inst) {

    }

    @Override
    public void visit(IRIcmpInstruction inst) {

    }

    @Override
    public void visit(IRTruncInstruction inst) {

    }

    @Override
    public void visit(IRZextInstruction inst) {

    }

    @Override
    public void visit(IRGetelementptrInstruction inst) {

    }

    @Override
    public void visit(IRBitcastInstruction inst) {

    }
}

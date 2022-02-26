package MiddleEnd;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.IRModule;
import IR.Instruction.*;
import IR.Operand.IRGlobalVariableRegister;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;
import Utility.CloneManager;
import Utility.error.OptimizeError;

import java.util.*;

import static Debug.MemoLog.log;

public class FunctionInliner implements IRFunctionPass {
    static private final int INSTRUCTION_LIMITATION = 500, BLOCK_LIMITATION = 50;
    private static int cnt = 0;

    private boolean changed = false;
    private final LinkedHashSet<IRFunction> couldInline = new LinkedHashSet<>();
    private final LinkedHashMap<IRFunction, Integer> instNum = new LinkedHashMap<>();
    private final ArrayList<IRCallInstruction> toBeOptimized = new ArrayList<>();
    private final LinkedHashMap<IROperand, IROperand> operandClone = new LinkedHashMap<>();
    private final LinkedHashMap<IRBasicBlock, IRBasicBlock> blockClone = new LinkedHashMap<>();

    public boolean inline(Memory memory) {
        Collection<IRFunction> functions = memory.getIRModule().getFunctions().values();
        couldInline.addAll(functions);
        couldInline.remove(memory.getIRModule().getMainFunction());
        functions.forEach(this::visit);
        log.Debugf("couldInline: %s\n", couldInline);
        functions.forEach(func -> func.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst instanceof IRCallInstruction && couldInline.contains(((IRCallInstruction) inst).getCallFunction())) {
                toBeOptimized.add((IRCallInstruction) inst);
                changed = true;
            }
        })));
        log.Debugf("toBeOptimized: %s\n", toBeOptimized);
        toBeOptimized.forEach(this::inlineFunction);
        memory.getIRModule().removeUnusedFunction();
        if (changed) log.Infof("Program changed in inline.\n");
        return changed;
    }

    private boolean inlineAvailable(IRFunction function) {
        return function.getBlocks().size() <= BLOCK_LIMITATION && instNum.get(function) <= INSTRUCTION_LIMITATION;
    }

    private IROperand cloneOperand(IROperand operand) {
        if (!operandClone.containsKey(operand)) {
            IROperand c;
            if (operand instanceof IRRegister && !(operand instanceof IRGlobalVariableRegister)) {
                c = new IRRegister(operand.getIRType(), ((IRRegister) operand).getName() + "_clone");
            } else c = operand;
            operandClone.put(operand, c);
        }
        return operandClone.get(operand);
    }

    private void inlineFunction(IRCallInstruction call) {
        IRFunction funcToInline = call.getCallFunction();
        if (!inlineAvailable(funcToInline)) return;
        IRBasicBlock parentBlock = call.getParentBlock();
        IRFunction parentFunc = parentBlock.getParentFunction();
        IRBasicBlock inlineSplit = new IRBasicBlock(parentFunc, "inline_split_" + (++cnt));
        if (parentBlock.isReturnBlock()) {
            parentBlock.markReturnBlock(false);
            inlineSplit.markReturnBlock(true);
            parentFunc.setReturnBlock(inlineSplit);
        }
        ArrayList<IRBasicBlock> successors = new ArrayList<>(parentBlock.getSuccessors());
        successors.forEach(succ -> {
            succ.removePredecessor(parentBlock);
            succ.addPredecessor(inlineSplit);
            parentBlock.removeSuccessor(succ);
            succ.getLabel().removeUser(parentBlock.getEscapeInstruction());
            succ.getPhis().forEach(phi -> phi.replaceSourceBlock(parentBlock, inlineSplit));
        });
        ArrayList<IRInstruction> instructions = new ArrayList<>(parentBlock.getInstructions());
        int indexOfCall = instructions.indexOf(call);
        call.removeFromParentBlock();
        for (int i = indexOfCall + 1; i < instructions.size(); i++) {
            IRInstruction inst = instructions.get(i);
            parentBlock.getInstructions().remove(inst);
            inst.setParentBlock(inlineSplit);
            if (inst == parentBlock.getEscapeInstruction()) inlineSplit.setEscapeInstruction(inst);
            else inlineSplit.appendInstruction(inst);
        }
        inlineSplit.finishBlock();
        CloneManager manager = new CloneManager(operandClone, blockClone);
        operandClone.clear();
        blockClone.clear();
        for (int i = 0; i < funcToInline.getParameters().size(); i++) {
            operandClone.put(funcToInline.getParameters().get(i), call.getArgumentValues().get(i));
        }
        funcToInline.getBlocks().forEach(block -> blockClone.put(block, new IRBasicBlock(parentFunc, block.getLabelWithFunctionName() + "_clone_" + (++cnt))));
        funcToInline.getBlocks().forEach(block -> {
            IRBasicBlock cBlock = blockClone.get(block);
            block.getInstructions().forEach(inst -> {
                IRInstruction cInst;
                if (inst instanceof IRReturnInstruction) {
                    cInst = new IRJumpInstruction(cBlock, inlineSplit);
                    if (call.haveReturnValue()) {
                        IROperand retValClone = cloneOperand(((IRReturnInstruction) inst).getReturnValue());
                        IROperand retVal = call.getResultRegister();
                        LinkedHashSet<IRInstruction> users = new LinkedHashSet<>(retVal.getUsers());
                        users.forEach(user -> user.replaceUse(retVal, retValClone));
                    }
                } else {
                    inst.forEachNonLabelOperand(this::cloneOperand);
                    cInst = inst.cloneMySelf(manager);
                }
                if (inst == block.getEscapeInstruction()) cBlock.setEscapeInstruction(cInst);
                else {
                    cBlock.appendInstruction(cInst);
                    if (cInst instanceof IRPhiInstruction) cBlock.addPhi((IRPhiInstruction) cInst);
                    if (cInst instanceof IRAllocaInstruction) throw new OptimizeError("unexpected alloca in inline (should eliminated in mem2reg)");
                }
            });
            cBlock.finishBlock();
            blockClone.put(block, cBlock);
        });
        IRJumpInstruction jumpToNewEntry = new IRJumpInstruction(parentBlock, blockClone.get(funcToInline.getEntryBlock()));
        parentBlock.appendInstructionWithoutCheck(jumpToNewEntry);
        parentBlock.setEscapeInstructionWithoutCheck(jumpToNewEntry);
        ArrayList<IRBasicBlock> newBlocks = new ArrayList<>();
        int indexOfCallBlock = parentFunc.getBlocks().indexOf(parentBlock);
        for (int i = 0; i <= indexOfCallBlock; i++) newBlocks.add(parentFunc.getBlocks().get(i));
        blockClone.forEach((ori, clone) -> newBlocks.add(clone));
        newBlocks.add(inlineSplit);
        for (int i = indexOfCallBlock + 1; i < parentFunc.getBlocks().size(); i++) newBlocks.add(parentFunc.getBlocks().get(i));
        parentFunc.setBlocks(newBlocks);
        visit(parentFunc);
    }

    @Override
    public void visit(IRFunction function) { // evaluate
        int instCnt = 0;
        for (IRBasicBlock block : function.getBlocks()) {
            for (IRInstruction inst : block.getInstructions()) {
                instCnt++;
                if (inst instanceof IRCallInstruction) couldInline.remove(function);
            }
        }
        instNum.put(function, instCnt);
    }
}

package MiddleEnd;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.Instruction.*;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;
import MiddleEnd.Utils.DominatorTreeBuilder;
import Utility.error.OptimizeError;

import java.util.*;

import static Debug.MemoLog.log;

public class MemoryToRegisterPromoter implements IRFunctionPass {
    private IRFunction function;
    private final LinkedHashSet<IRAllocaInstruction> allocas = new LinkedHashSet<>();
    private final LinkedHashMap<IRPhiInstruction, IRAllocaInstruction> phi2alloca = new LinkedHashMap<>();
    private final LinkedHashMap<IRRegister, ArrayList<IRBasicBlock>> allocaUserBlocks = new LinkedHashMap<>();

    public void promote(Memory memory) {
        memory.getIRModule().getFunctions().values().forEach(this::visit);
    }

    private void resetAllocas() {
        allocas.clear();
        allocas.addAll(function.getEntryBlock().getAllocas());
    }

    private void initialize() {
        resetAllocas();
        phi2alloca.clear();
        allocaUserBlocks.clear();
        allocas.forEach(alloca -> {
            ArrayList<IRBasicBlock> userBlocks = new ArrayList<>();
            alloca.getAllocaTarget().getUsers().forEach(inst -> {
                if (!userBlocks.contains(inst.getParentBlock())) userBlocks.add(inst.getParentBlock());
            });
            allocaUserBlocks.put(alloca.getAllocaTarget(), userBlocks);
        });
        this.dominatorFrontier = function.getDominatorFrontier();
        Aorig.clear();
        defsites.clear();
        function.getBlocks().forEach(block -> Aorig.put(block, new LinkedHashSet<>()));
    }

    private void basicOptimize(IRAllocaInstruction alloca) {
        ArrayList<IRBasicBlock> userBlocks = allocaUserBlocks.get(alloca.getAllocaTarget());
        ArrayList<IRLoadInstruction> userLoad = new ArrayList<>();
        ArrayList<IRStoreInstruction> userStore = new ArrayList<>();
        for (IRInstruction inst : alloca.getAllocaTarget().getUsers()) {
            if (inst instanceof IRLoadInstruction) userLoad.add((IRLoadInstruction) inst);
            else if (inst instanceof IRStoreInstruction) userStore.add((IRStoreInstruction) inst);
            else throw new OptimizeError("alloca instruction with user that not load or store");
        }
        if (userBlocks.size() == 0) alloca.removeFromParentBlock();
        else if (userStore.size() == 1) {
            IRStoreInstruction storeInst = userStore.get(0);
            IROperand storeValue = storeInst.getStoreValue();
            for (IRLoadInstruction load : userLoad) {
                LinkedHashSet<IRInstruction> users = new LinkedHashSet<>(load.getResultRegister().getUsers());
                for (IRInstruction user : users) user.replaceUse(load.getResultRegister(), storeValue);
                load.removeFromParentBlock();
            }
            storeInst.removeFromParentBlock();
            alloca.removeFromParentBlock();
        } else if (userBlocks.size() == 1) {
            IRBasicBlock block = userBlocks.get(0);
            IROperand memoryAddress = alloca.getAllocaTarget();
            IROperand currentValue = null;
            ArrayList<IRInstruction> insts = new ArrayList<>(block.getInstructions());
            for (IRInstruction inst : insts) {
                if (inst instanceof IRLoadInstruction && ((IRLoadInstruction) inst).getLoadAddress() == memoryAddress) {
                    assert currentValue != null;
                    IRRegister loadReg = ((IRLoadInstruction) inst).getResultRegister();
                    for (IRInstruction user : loadReg.getUsers()) user.replaceUse(loadReg, currentValue);
                    inst.removeFromParentBlock();
                }
                if (inst instanceof IRStoreInstruction && ((IRStoreInstruction) inst).getStoreAddress() == memoryAddress) {
                    currentValue = ((IRStoreInstruction) inst).getStoreValue();
                    inst.removeFromParentBlock();
                }
            }
            alloca.removeFromParentBlock();
        }
    }

    private final LinkedHashMap<IRBasicBlock, LinkedHashSet<IRAllocaInstruction>> Aorig = new LinkedHashMap<>();
    private final LinkedHashMap<IRAllocaInstruction, LinkedHashSet<IRBasicBlock>> defsites = new LinkedHashMap<>();
    private LinkedHashMap<IRBasicBlock, LinkedHashSet<IRBasicBlock>> dominatorFrontier;

    private void placePhi() {
        IRRegister.reset();
        allocas.forEach(a -> defsites.put(a, new LinkedHashSet<>()));
        // find all defs for alloca
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            // only store inst def alloca
            if (inst instanceof IRStoreInstruction) {
                IRAllocaInstruction def = ((IRStoreInstruction) inst).getStoreAddress().getAllocaDef();
                if (allocas.contains(def)) Aorig.get(block).add(def);
            }
        }));
        function.getBlocks().forEach(n -> Aorig.get(n).forEach(a -> defsites.get(a).add(n)));
        allocas.forEach(a -> {
            Queue<IRBasicBlock> workList = new LinkedList<>(defsites.get(a));
            LinkedHashSet<IRBasicBlock> visited = new LinkedHashSet<>();
            while (!workList.isEmpty()) {
                IRBasicBlock n = workList.poll();
                assert dominatorFrontier.get(n) != null : n + " is not in dominatorFrontier";
                dominatorFrontier.get(n).forEach(Y -> {
                    if (visited.contains(Y)) return;
                    visited.add(Y);
                    IRPhiInstruction phi = new IRPhiInstruction(Y, new IRRegister(a.getAllocaType(), "phi_" + a.getAllocaTarget().getName()), a.getAllocaType());
                    log.Tracef("placing phi [%s] for alloca [%s] in BasicBlock [%s]\n", phi, a, Y);
                    phi2alloca.put(phi, a);
                    Y.addPhi(phi);
                    workList.offer(Y);
                });
            }
        });
    }

    private final LinkedHashMap<IRAllocaInstruction, Stack<IROperand>> stack = new LinkedHashMap<>();

    private void initRename() {
        stack.clear();
        allocas.forEach(a -> {
            Stack<IROperand> aStack = new Stack<>();
            stack.put(a, aStack);
        });
    }

    private void rename(IRBasicBlock block) {
        log.Tracef("start renaming block [%s]\n", block);
        LinkedHashSet<IRAllocaInstruction> defer = new LinkedHashSet<>();
        block.getPhis().forEach(phi -> {
            IRAllocaInstruction a = phi2alloca.get(phi);
            stack.get(a).push(phi.getResultRegister());
            log.Tracef("push %s to %s\n", phi.getResultRegister(), a);
            defer.add(a);
        });
        ArrayList<IRInstruction> instructions = new ArrayList<>(block.getInstructions());
        instructions.forEach(inst -> {
            if (inst instanceof IRLoadInstruction) { // only load inst use alloca
                IRAllocaInstruction x = ((IRLoadInstruction) inst).getLoadAddress().getAllocaDef();
                if (!allocas.contains(x)) return;
                assert !stack.get(x).isEmpty();
                IROperand val = stack.get(x).peek();
                inst.replaceAllUseWithValue(val);
                inst.removeFromParentBlock();
            } else if (inst instanceof IRStoreInstruction) {
                IRAllocaInstruction a = ((IRStoreInstruction) inst).getStoreAddress().getAllocaDef();
                if (!allocas.contains(a)) return;
                if (defer.contains(a)) stack.get(a).pop();
                else defer.add(a);
                stack.get(a).push(((IRStoreInstruction) inst).getStoreValue());
                log.Tracef("push %s to %s\n", ((IRStoreInstruction) inst).getStoreValue(), a);
                inst.removeFromParentBlock();
            } else if (inst instanceof IRAllocaInstruction) inst.removeFromParentBlock();
        });
        block.getSuccessors().forEach(succ -> {
            log.Tracef("add phi candidate for %s\n", succ);
            succ.getPhis().forEach(phi -> {
                log.Tracef("manage phi: %s\n", phi);
                assert phi2alloca.containsKey(phi);
                IRAllocaInstruction a = phi2alloca.get(phi);
                phi.addCandidate(stack.get(a).isEmpty() ? phi.getResultRegister() : stack.get(a).peek(), block);
            });
        });
        block.getDominatorTreeSuccessors().forEach(this::rename);
        defer.forEach(a -> stack.get(a).pop());
    }

    @Override
    public void visit(IRFunction function) {
        new DominatorTreeBuilder().build(function, false);
        this.function = function;
        initialize();
        allocas.forEach(this::basicOptimize);
        resetAllocas();
        placePhi();
        initRename();
        rename(function.getEntryBlock());
        function.relocatePhis();
    }
}

package MiddleEnd;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.Instruction.*;
import IR.Operand.IRNull;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import Memory.Memory;
import Utility.error.OptimizeError;

import java.util.*;

public class MemoryToRegisterPromoter extends IROptimize {
    private IRFunction function;
    private final LinkedHashSet<IRAllocaInstruction> allocas = new LinkedHashSet<>();
    private final LinkedHashMap<IRPhiInstruction, IRAllocaInstruction> phi2alloca = new LinkedHashMap<>();
    private final LinkedHashMap<IRRegister, ArrayList<IRBasicBlock>> allocaUserBlocks = new LinkedHashMap<>();

    public void promote(Memory memory) {
        if (doOptimize) {
            memory.getIRModule().getFunctions().values().forEach(this::visit);
        }
    }

    private void resetAllocas() {
        allocas.clear();
        allocas.addAll(function.getEntryBlock().getAllocas());
    }

    private void initialize() {
        resetAllocas();
        allocaUserBlocks.clear();
        allocas.forEach(alloca -> {
            ArrayList<IRBasicBlock> userBlocks = new ArrayList<>();
            alloca.getAllocaTarget().getUsers().forEach(inst -> {
                if (!userBlocks.contains(inst.getParentBlock())) userBlocks.add(inst.getParentBlock());
            });
            allocaUserBlocks.put(alloca.getAllocaTarget(), userBlocks);
        });
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
    private final LinkedHashMap<IRBasicBlock, LinkedHashSet<IRAllocaInstruction>> Aphi = new LinkedHashMap<>();
    private final LinkedHashMap<IRAllocaInstruction, LinkedHashSet<IRBasicBlock>> defsites = new LinkedHashMap<>();
    private LinkedHashMap<IRBasicBlock, LinkedHashSet<IRBasicBlock>> dominatorFrontier;

    private void initPlacingPhi() {
        this.dominatorFrontier = function.getDominatorFrontier();
        Aorig.clear();
        Aphi.clear();
        defsites.clear();
        function.getBlocks().forEach(block -> {
            Aorig.put(block, new LinkedHashSet<>());
            Aphi.put(block, new LinkedHashSet<>());
            block.getInstructions().forEach(inst -> {
                // only store inst def alloca
                if (inst instanceof IRStoreInstruction) {
                    IRInstruction def = ((IRStoreInstruction) inst).getStoreValue().getDef();
                    if (def instanceof IRAllocaInstruction && allocas.contains(def)) {
                        Aorig.get(block).add((IRAllocaInstruction) def);
                    }
                }
            });
        });
        allocas.forEach(a -> defsites.put(a, new LinkedHashSet<>()));
    }

    private void placePhi() {
        function.getBlocks().forEach(n -> Aorig.get(n).forEach(a -> defsites.get(a).add(n)));
        allocas.forEach(a -> {
            Queue<IRBasicBlock> workList = new LinkedList<>(defsites.get(a));
            while (!workList.isEmpty()) {
                IRBasicBlock n = workList.poll();
                dominatorFrontier.get(n).forEach(Y -> {
                    if (!Aphi.get(Y).contains(a)) {
                        IRPhiInstruction phi = new IRPhiInstruction(Y, new IRRegister(a.getAllocaType(), "phi"), a.getAllocaType());
                        phi2alloca.put(phi, a);
                        Y.addPhi(phi);
                        Aphi.get(Y).add(a);
                        if (!Aorig.get(n).contains(a)) workList.offer(Y);
                    }
                });
            }
        });
    }

    private final LinkedHashMap<IRAllocaInstruction, Stack<IROperand>> stack = new LinkedHashMap<>();

    private void initRename() {
        stack.clear();
        allocas.forEach(a -> stack.put(a, new Stack<>()));
    }

    private void rename(IRBasicBlock block) {
        LinkedHashSet<IRAllocaInstruction> defer = new LinkedHashSet<>();
        phi2alloca.forEach((phi, a) -> {
            stack.get(a).add(phi.getResultRegister());
            defer.add(a);
        });
        ArrayList<IRInstruction> instructions = new ArrayList<>(block.getInstructions());
        instructions.forEach(inst -> {
            // only load inst use alloca
            if (inst instanceof IRLoadInstruction) {
                IRAllocaInstruction x = (IRAllocaInstruction) ((IRLoadInstruction) inst).getLoadAddress().getDef();
                assert allocas.contains(x) : x;
                IROperand val = stack.get(x).isEmpty() ? new IRNull(null) : stack.get(x).peek();
                ((IRLoadInstruction) inst).getResultRegister().getUsers().forEach(user -> user.replaceUse(((IRLoadInstruction) inst).getResultRegister(), val));
                inst.removeFromParentBlock();
            } else if (inst instanceof IRStoreInstruction) {
                IRAllocaInstruction a = (IRAllocaInstruction) ((IRStoreInstruction) inst).getStoreAddress().getDef();
                assert allocas.contains(a) : a;
                stack.get(a).push(((IRStoreInstruction) inst).getStoreValue());
                defer.add(a);
                inst.removeFromParentBlock();
            } else if (inst instanceof IRAllocaInstruction) inst.removeFromParentBlock();
        });
        block.getSuccessors().forEach(succ -> succ.getPhis().forEach(phi -> {
            assert phi2alloca.containsKey(phi);
            phi.addCandidate(stack.get(phi2alloca.get(phi)).peek(), block);
        }));
        block.getDominatorTreePredecessors().forEach(this::rename);
        defer.forEach(a -> stack.get(a).pop());
    }

    @Override
    protected void visit(IRFunction function) {
        this.function = function;
        initialize();
        for (IRAllocaInstruction alloca : allocas) basicOptimize(alloca);
//        resetAllocas();
//        initPlacingPhi();
//        placePhi();
//        initRename();
//        rename(function.getEntryBlock());
    }
}

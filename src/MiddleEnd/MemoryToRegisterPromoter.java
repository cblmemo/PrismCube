package MiddleEnd;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.Instruction.IRAllocaInstruction;
import IR.Instruction.IRInstruction;
import IR.Instruction.IRLoadInstruction;
import IR.Instruction.IRStoreInstruction;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import Memory.Memory;
import Utility.error.OptimizeError;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class MemoryToRegisterPromoter extends Optimize {
    private IRFunction function;
    private final LinkedHashSet<IRAllocaInstruction> allocas = new LinkedHashSet<>();
    private final LinkedHashMap<IRRegister, ArrayList<IRBasicBlock>> allocaUserBlocks = new LinkedHashMap<>();

    public void promote(Memory memory) {
        if (doOptimize) {
            memory.getIRModule().getFunctions().values().forEach(this::visit);
        }
    }

    private void initialize() {
        allocas.clear();
        allocas.addAll(function.getEntryBlock().getAllocas());
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
                LinkedHashSet<IRInstruction> users = new LinkedHashSet<>(load.getLoadTarget().getUsers());
                for (IRInstruction user : users) user.replaceUse(load.getLoadTarget(), storeValue);
                load.removeFromParentBlock();
            }
            storeInst.removeFromParentBlock();
            alloca.removeFromParentBlock();
        } else if (userBlocks.size() == 1) {
            IRBasicBlock block = userBlocks.get(0);
            assert block == block.getParentFunction().getEntryBlock() : "alloca not in entry block";
            IROperand memoryAddress = alloca.getAllocaTarget();
            IROperand currentValue = null;
            ArrayList<IRInstruction> insts = new ArrayList<>(block.getInstructions());
            for (IRInstruction inst : insts) {
                if (inst instanceof IRLoadInstruction && ((IRLoadInstruction) inst).getLoadValue() == memoryAddress) {
                    assert currentValue != null;
                    IRRegister loadReg = ((IRLoadInstruction) inst).getLoadTarget();
                    for (IRInstruction user : loadReg.getUsers()) user.replaceUse(loadReg, currentValue);
                    inst.removeFromParentBlock();
                }
                if (inst instanceof IRStoreInstruction && ((IRStoreInstruction) inst).getStoreTarget() == memoryAddress) {
                    currentValue = ((IRStoreInstruction) inst).getStoreValue();
                    inst.removeFromParentBlock();
                }
            }
            alloca.removeFromParentBlock();
        }
    }

    @Override
    protected void visit(IRFunction function) {
        this.function = function;
        initialize();
        for (IRAllocaInstruction alloca : allocas) basicOptimize(alloca);
    }
}

package BackEnd.RegisterAllocate;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Operand.ASMRegister;

import java.util.*;

public class LivenessAnalyzer {
    private final ASMFunction function;
    private final LinkedHashMap<ASMBasicBlock, LinkedHashSet<ASMRegister>> def = new LinkedHashMap<>();
    private final LinkedHashMap<ASMBasicBlock, LinkedHashSet<ASMRegister>> use = new LinkedHashMap<>();
    private final LinkedHashMap<ASMBasicBlock, LinkedHashSet<ASMRegister>> liveIn = new LinkedHashMap<>();
    private final LinkedHashMap<ASMBasicBlock, LinkedHashSet<ASMRegister>> liveOut = new LinkedHashMap<>();

    public LivenessAnalyzer(ASMFunction function) {
        this.function = function;
    }

    private void constructBlockDefUse() {
        function.getBlocks().forEach(block -> {
            LinkedHashSet<ASMRegister> blockDef = new LinkedHashSet<>();
            LinkedHashSet<ASMRegister> blockUse = new LinkedHashSet<>();
            block.getInstructions().forEach(inst -> {
                if (inst == null) return;
                // LinkedHashSet will automatically check whether reg in set
                blockDef.addAll(inst.getDefs());
                blockUse.addAll(inst.getUses());
            });
            def.put(block, blockDef);
            use.put(block, blockUse);
        });
    }

    private void constructLiveInOut() {
        function.getBlocks().forEach(block -> {
            liveIn.put(block, new LinkedHashSet<>());
            liveOut.put(block, new LinkedHashSet<>());
        });
        boolean flag;
        do {
            flag = false;
            ArrayList<ASMBasicBlock> blocks = function.getTopologicalOrder();
            for (ASMBasicBlock block : blocks) {
                int oldInSize = liveIn.get(block).size(), oldOutSize = liveOut.get(block).size();
                LinkedHashSet<ASMRegister> newIn = new LinkedHashSet<>(use.get(block)), newOut = new LinkedHashSet<>();
                liveOut.get(block).forEach(reg -> {
                    if (!def.get(block).contains(reg)) newIn.add(reg);
                });
                block.getSuccessors().forEach(successorBlock -> newOut.addAll(liveIn.get(successorBlock)));
                liveIn.replace(block, newIn);
                liveOut.replace(block, newOut);
                flag |= !(newIn.size() == oldInSize && newOut.size() == oldOutSize);
            }
        } while (flag);
        function.getBlocks().forEach(block -> block.setLiveOut(liveOut.get(block)));
    }

    public void analyze() {
        constructBlockDefUse();
        constructLiveInOut();
    }
}

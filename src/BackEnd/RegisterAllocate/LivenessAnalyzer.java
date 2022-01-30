package BackEnd.RegisterAllocate;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Operand.ASMRegister;

import java.util.*;

import static Debug.MemoLog.log;

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
                inst.getUses().forEach(u -> {
                    if (!blockDef.contains(u)) blockUse.add(u);
                });
                blockDef.addAll(inst.getDefs());
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
        ArrayList<ASMBasicBlock> blocks = function.getTopologicalOrder();
        do {
            flag = false;
            for (ASMBasicBlock block : blocks) {
                int oldInSize = liveIn.get(block).size(), oldOutSize = liveOut.get(block).size();
                LinkedHashSet<ASMRegister> newOut = new LinkedHashSet<>();
                block.getSuccessors().forEach(successorBlock -> newOut.addAll(liveIn.get(successorBlock)));
                LinkedHashSet<ASMRegister> newIn = new LinkedHashSet<>(newOut);
                newIn.removeAll(def.get(block));
                newIn.addAll(use.get(block));
                liveIn.replace(block, newIn);
                liveOut.replace(block, newOut);
                flag |= !(newIn.size() == oldInSize && newOut.size() == oldOutSize);
            }
        } while (flag);
        function.getBlocks().forEach(block -> block.setLiveOut(liveOut.get(block)));
    }

    public void analyze() {
        log.Debugf("start liveness analyze.\n");
        constructBlockDefUse();
        constructLiveInOut();
        log.Debugf("liveness analyze finished.\n");
    }
}

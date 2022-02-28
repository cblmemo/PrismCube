package MiddleEnd.ASMOptimize;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import Memory.Memory;
import MiddleEnd.Pass.ASMFunctionPass;

import java.util.ArrayList;

public class BlockReorderer implements ASMFunctionPass {
    private ASMFunction function;
    private final ArrayList<ASMBasicBlock> brandNewOrder = new ArrayList<>();

    public void reorder(Memory memory) {
        memory.getAsmModule().getFunctions().values().forEach(func -> new BlockReorderer().visit(func));
    }

    private void addToBrandNewOrder(ASMBasicBlock block) {
        if (brandNewOrder.contains(block) || !block.endWithJump()) return;
        brandNewOrder.add(block);
        addToBrandNewOrder(block.getJumpTarget());
    }

    private void naiveReorder() {
        function.getBlocks().forEach(this::addToBrandNewOrder);
        // only return block end with ret ( not jump )
        brandNewOrder.add(function.getReturnBlock());
        assert function.getBlocks().size() == brandNewOrder.size();
    }

    private void eliminateRedundantJump() {
        ArrayList<ASMBasicBlock> blocks = function.getBlocks();
        for (int i = 0; i < blocks.size() - 1; i++)
            if (blocks.get(i).getJumpTarget() == blocks.get(i + 1)) blocks.get(i).removeTailJump();
    }

    @Override
    public void visit(ASMFunction function) {
        this.function = function;
        naiveReorder();
        function.setBlocks(brandNewOrder);
        eliminateRedundantJump();
    }
}

package MiddleEnd.IROptimize;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.Instruction.IRJumpInstruction;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import static Debug.MemoLog.log;

/**
 * This class fuse directly linked blocks and remove unreachable blocks.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class IRBlockFuser implements IRFunctionPass {
    private boolean changed = false;

    public boolean fuse(Memory memory) {
        memory.getIRModule().getFunctions().values().forEach(this::visit);
        memory.getIRModule().getFunctions().values().forEach(this::visit);
        memory.getIRModule().removeUnusedFunction();
        if (changed) log.Infof("Program changed in fuse.\n");
        return changed;
    }

    @Override
    public void visit(IRFunction function) {
        // remove unreachable blocks
        changed |= function.removeUnreachableBlocks();
        // cannot fuse entry block and exit block
        if (function.getBlocks().size() <= 2) return;
        // fuse blocks
        ArrayList<IRBasicBlock> blocks = new ArrayList<>(function.getBlocks());
        LinkedHashSet<IRBasicBlock> deleted = new LinkedHashSet<>();
        blocks.forEach(pred -> {
            if (!deleted.contains(pred) && pred.getEscapeInstruction() instanceof IRJumpInstruction) {
                assert pred.getSuccessors().size() == 1 : "jump: [" + pred.getEscapeInstruction() + "], succ: [" + pred.getSuccessors() + "]";
                IRBasicBlock succ = pred.getSuccessors().get(0);
                if (succ.getPredecessors().size() == 1) {
                    assert succ.getPredecessors().get(0) == pred;
                    pred.fuse(succ);
                    function.getBlocks().remove(succ);
                    // ensure return block is the last element of blocks
                    if (pred.isReturnBlock()) function.relocateReturnBlock(pred);
                    deleted.add(succ);
                    changed = true;
                }
            }
        });
    }
}

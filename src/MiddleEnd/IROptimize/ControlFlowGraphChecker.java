package MiddleEnd.IROptimize;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.Instruction.IRBrInstruction;
import IR.Instruction.IRInstruction;
import IR.Instruction.IRJumpInstruction;
import IR.Instruction.IRReturnInstruction;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;

import java.util.ArrayList;

import static Debug.MemoLog.log;

public class ControlFlowGraphChecker implements IRFunctionPass {
    private final String errorMessage;

    public ControlFlowGraphChecker(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void check(Memory memory) {
        log.Tracef("Start check CFG.\n");
        memory.getIRModule().getFunctions().values().forEach(this::visit);
        log.Tracef("Check finished. Bug not found.\n");
    }

    private void check(IRBasicBlock pred, IRBasicBlock succ) {
        if (!pred.getSuccessors().contains(succ)) log.Fatalf("succ [%s] is not in successors [%s] of [%s]\n", succ, pred.getSuccessors(), pred);
        if (!succ.getPredecessors().contains(pred)) log.Fatalf("pred [%s] is not in predecessors [%s] of [%s]\n", pred, succ.getPredecessors(), succ);
    }

    private boolean containsOtherThan(ArrayList<IRBasicBlock> blocks, IRBasicBlock tar0) {
        for (IRBasicBlock b : blocks) {
            if (b != tar0) return true;
        }
        return false;
    }

    private boolean containsOtherThan(ArrayList<IRBasicBlock> blocks, IRBasicBlock tar0, IRBasicBlock tar1) {
        for (IRBasicBlock b : blocks) {
            if (b != tar0 && b != tar1) return true;
        }
        return false;
    }

    @Override
    public void visit(IRFunction function) {
        function.getBlocks().forEach(block -> {
            IRInstruction escInst = block.getEscapeInstruction();
            if (escInst instanceof IRBrInstruction) {
                IRBasicBlock t = ((IRBrInstruction) escInst).getThenBlock(), e = ((IRBrInstruction) escInst).getElseBlock();
                check(block, t);
                check(block, e);
                if (containsOtherThan(block.getSuccessors(), t, e)) log.Fatalf("block has redundant successors. succ: [%s], then: [%s], else: [%s]\n", block.getSuccessors(), t, e);
            } else if (escInst instanceof IRJumpInstruction) {
                IRBasicBlock t = ((IRJumpInstruction) escInst).getTargetBlock();
                check(block, t);
                if (containsOtherThan(block.getSuccessors(), t)) log.Fatalf("block has redundant successors. succ: [%s], target: [%s]\n", block.getSuccessors(), t);
            } else if (!(escInst instanceof IRReturnInstruction)) log.Fatalf("escInst [%s] is neither br nor jump with error message: [%s]\n", escInst, errorMessage);
        });
    }
}

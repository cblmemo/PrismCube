package MiddleEnd.ASMOptimize;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Instruction.ASMInstruction;
import ASM.Operand.ASMLabel;
import Memory.Memory;
import MiddleEnd.Pass.ASMFunctionPass;

import java.util.LinkedHashSet;

/**
 * This class pull code in return block to their
 * predecessors, therefore a direct jump was saved.
 * <br>Notice that this pass will expand code size,
 * therefore a threshold is set to avoid it.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class CodePuller implements ASMFunctionPass {
    private static final int rounds = 10;

    private ASMFunction function;
    private final LinkedHashSet<ASMBasicBlock> returnBlocks = new LinkedHashSet<>();

    public void pull(Memory memory) {
        memory.getAsmModule().getFunctions().values().forEach(this::visit);
    }

    private void iteration() {
        function.getBlocks().forEach(block -> {
            if (block.endWithJump()) {
                ASMBasicBlock jumpTarget = block.getJumpTarget();
                if (returnBlocks.contains(jumpTarget) || returnBlocks.contains(block.getTailBranchTarget())) {
                    if (returnBlocks.contains(block.getTailBranchTarget()) && !returnBlocks.contains(jumpTarget)) {
                        block.swapTailBranch();
                        jumpTarget = block.getJumpTarget();
                    }
                    block.removeTailJump();
                    jumpTarget.getInstructions().forEach(inst -> {
                        ASMInstruction clone = inst.cloneMySelf();
                        block.appendInstruction(clone);
                        if (clone.isBranch()) {
                            assert clone.getOperands().get(1) instanceof ASMLabel;
                            ASMBasicBlock branchTarget = ((ASMLabel) clone.getOperands().get(1)).belongTo();
                            branchTarget.addPredecessor(block);
                            block.addSuccessor(branchTarget);
                        }
                    });
                    block.removeSuccessor(jumpTarget);
                    jumpTarget.removePredecessor(block);
                }
            }
        });
        returnBlocks.forEach(rmv -> {
            if (rmv.getPredecessors().isEmpty() && function.getEntryBlock() != rmv) function.getBlocks().remove(rmv);
        });
        returnBlocks.clear();
        function.getBlocks().forEach(block -> {
            if (block.isPullableReturnBlock()) returnBlocks.add(block);
        });
    }

    @Override
    public void visit(ASMFunction function) {
        this.function = function;
        returnBlocks.clear();
        returnBlocks.add(function.getReturnBlock());
        int cnt = 0;
        while (cnt++ < rounds) iteration();
    }
}

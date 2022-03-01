package MiddleEnd.ASMOptimize;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Instruction.ASMInstruction;
import ASM.Instruction.ASMPseudoInstruction;
import ASM.Operand.ASMImmediate;
import ASM.Operand.ASMLabel;
import Memory.Memory;
import MiddleEnd.Pass.ASMFunctionPass;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import static Debug.MemoLog.log;

/**
 * This class simplify control flows inside asm blocks.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class ControlFlowSimplifyer implements ASMFunctionPass {
    private boolean changed = false;
    private ASMFunction function;

    public boolean simplify(Memory memory) {
        memory.getAsmModule().getFunctions().values().forEach(this::visit);
        log.Infof("program changed in control flow simplify.\n");
        return changed;
    }

    private void convertConstRegisterBranchToJump(ASMBasicBlock block) {
        ArrayList<ASMInstruction> instructions = new ArrayList<>(block.getInstructions());
        for (int i = 1; i < instructions.size(); i++) {
            ASMInstruction pre = instructions.get(i - 1), now = instructions.get(i);
            if (pre.isLi() && now.isBeqz()) {
                if (pre.getOperands().get(0) == now.getOperands().get(0)) {
                    assert pre.getOperands().get(1) instanceof ASMImmediate;
                    int branchTarget = ((ASMImmediate) pre.getOperands().get(1)).getImm();
                    ASMLabel brLabel = (ASMLabel) now.getOperands().get(1);
                    // beqz
                    if (branchTarget == 0) {
                        ASMInstruction jump = new ASMPseudoInstruction(block, ASMPseudoInstruction.InstType.j).addOperand(brLabel);
                        block.replaceInstruction(now, jump);
                    } else {
                        now.removeFromParentBlock();
                        block.removeSuccessor(brLabel.belongTo());
                        brLabel.belongTo().removePredecessor(block);
                    }
                    pre.removeFromParentBlock();
                    changed = true;
                }
            }
        }
    }

    /**
     * Deprecated because interfere with CodePuller.
     *
     * @see CodePuller
     * @deprecated
     */
    private void convertDirectlyJump() {
        LinkedHashMap<ASMBasicBlock, ASMBasicBlock> directlyJumpBlocks = new LinkedHashMap<>();
        function.getBlocks().forEach(block -> {
            if (block.isDirectlyJumpBlock()) directlyJumpBlocks.put(block, block.getDirectlyJumpTarget());
        });
        for (ASMBasicBlock block : function.getBlocks()) {
            ArrayList<ASMInstruction> instructions = new ArrayList<>(block.getInstructions());
            for (ASMInstruction inst : instructions) {
                if (inst.isJump()) {
                    ASMBasicBlock jumpTarget = ((ASMLabel) inst.getOperands().get(0)).belongTo();
                    if (directlyJumpBlocks.containsKey(jumpTarget)) {
                        ASMBasicBlock newTarget = directlyJumpBlocks.get(jumpTarget);
                        inst.getOperands().set(0, newTarget.getLabel());
                        changed = true;
                        block.removeSuccessor(jumpTarget);
                        block.addSuccessor(newTarget);
                        jumpTarget.removePredecessor(block);
                        newTarget.addPredecessor(block);
                    }
                }
            }
        }
        changed |= function.removeUnreachableBlocks();
    }

    private void fuseBlock() {
        ArrayList<ASMBasicBlock> blocks = new ArrayList<>(function.getBlocks());
        LinkedHashSet<ASMBasicBlock> deleted = new LinkedHashSet<>();
        blocks.forEach(pred -> {
            if (!deleted.contains(pred) && pred.withJumpEscapeInstruction()) {
                assert pred.getSuccessors().size() == 1;
                ASMBasicBlock succ = pred.getSuccessors().get(0);
                if (succ.getPredecessors().size() == 1) {
                    assert succ.getPredecessors().get(0) == pred;
                    pred.fuse(succ);
                    function.getBlocks().remove(succ);
                    deleted.add(succ);
                    changed = true;
                }
            }
        });
        log.Debugf("deleted blocks: %s\n", deleted);
    }

    @Override
    public void visit(ASMFunction function) {
        this.function = function;
        function.getBlocks().forEach(this::convertConstRegisterBranchToJump);
        fuseBlock();
    }
}

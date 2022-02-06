package MiddleEnd;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Instruction.ASMInstruction;
import ASM.Instruction.ASMMemoryInstruction;
import ASM.Instruction.ASMPseudoInstruction;
import ASM.Operand.ASMImmediate;
import ASM.Operand.ASMLabel;
import Memory.Memory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

public class PeepholePeeker extends ASMOptimize {
    private ASMFunction function;

    public void peek(Memory memory) {
        if (doOptimize) {
            memory.getAsmModule().getFunctions().values().forEach(this::visit);
        }
    }

    private void removeRedundantLoadStore() {
        function.getBlocks().forEach(block -> {
            for (int i = 1; i < block.getInstructions().size(); i++) {
                ASMInstruction pre = block.getInstructions().get(i - 1), now = block.getInstructions().get(i);
                if (pre instanceof ASMMemoryInstruction && now instanceof ASMMemoryInstruction) {
                    if (!pre.isStore() && now.isStore()) {
                        if (pre.getOperands().get(0) == now.getOperands().get(0) && Objects.equals(pre.getOperands().get(1), now.getOperands().get(1))) {
                            i--;
                            now.removeFromParentBlock();
                        }
                    }
                }
            }
        });
    }

    private void convertConstRegisterBranchToJump(ASMBasicBlock block) {
        ArrayList<ASMInstruction> instructions = new ArrayList<>(block.getInstructions());
        for (int i = 1; i < instructions.size(); i++) {
            ASMInstruction pre = instructions.get(i - 1), now = instructions.get(i);
            if (pre.isLi() && now.isBranch()) {
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
                }
            }
        }
    }

    private boolean removeUnreachableCode(ASMBasicBlock block) {
        ArrayList<ASMInstruction> instructions = new ArrayList<>(block.getInstructions());
        boolean remove = false, changed = false;
        for (ASMInstruction inst : instructions) {
            if (remove) {
                inst.removeFromParentBlock();
                changed = true;
            } else {
                if (inst.isJump()) remove = true;
            }
        }
        return changed;
    }

    private boolean changed = true;

    private void controlFlowOptimize() {
        changed = false;
        LinkedHashMap<ASMBasicBlock, ASMBasicBlock> directlyJumpBlocks = new LinkedHashMap<>();
        function.getBlocks().forEach(block -> {
            if (block.isDirectlyJumpBlock()) directlyJumpBlocks.put(block, block.getJumpTarget());
        });
        for (ASMBasicBlock block : function.getBlocks()) {
            ArrayList<ASMInstruction> instructions = new ArrayList<>(block.getInstructions());
            for (ASMInstruction inst : instructions) {
                if (inst.isJump()) {
                    ASMBasicBlock jumpTarget = ((ASMLabel) inst.getOperands().get(0)).belongTo();
                    if (directlyJumpBlocks.containsKey(jumpTarget)) {
                        inst.getOperands().set(0, directlyJumpBlocks.get(jumpTarget).getLabel());
                        changed = true;
                    }
                }
            }
        }
    }

    @Override
    protected void visit(ASMFunction function) {
        this.function = function;
        removeRedundantLoadStore();
        function.getBlocks().forEach(this::convertConstRegisterBranchToJump);
        function.getBlocks().forEach(this::removeUnreachableCode);
        function.removeUnreachableBlocks();
        while (changed) controlFlowOptimize();
    }
}

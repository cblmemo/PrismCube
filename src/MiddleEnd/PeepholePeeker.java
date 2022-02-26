package MiddleEnd;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Instruction.*;
import ASM.Operand.ASMImmediate;
import ASM.Operand.ASMLabel;
import Memory.Memory;
import MiddleEnd.Pass.ASMFunctionPass;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

public class PeepholePeeker implements ASMFunctionPass {
    static private final int rounds = 20;

    private ASMFunction function;

    public void peek(Memory memory) {
        memory.getAsmModule().getFunctions().values().forEach(this::visit);
    }

    private boolean safelyFold(ASMMoveInstruction mv0, ASMMoveInstruction mv1) {
        return mv0.getRd() == mv1.getRd() && mv0.getRs() == mv1.getRs();
    }

    private void foldMove(ASMBasicBlock block) {
        ArrayList<ASMInstruction> insts = new ArrayList<>(block.getInstructions());
        for (int i = 0; i < insts.size() - 1; i++) {
            ASMInstruction inst0 = insts.get(i), inst1 = insts.get(i + 1);
            if (inst0 instanceof ASMMoveInstruction && inst1 instanceof ASMMoveInstruction && safelyFold((ASMMoveInstruction) inst0, (ASMMoveInstruction) inst1)) {
                block.getInstructions().remove(inst0);
            }
        }
    }

    private boolean isFoldableAddi(ASMInstruction inst) {
        return inst.isAddi() && inst.getOperands().get(0) == inst.getOperands().get(1);
    }

    private boolean safelyFold(ASMArithmeticInstruction addi0, ASMArithmeticInstruction addi1) {
        return addi0.getOperands().get(0) == addi1.getOperands().get(0);
    }

    private void foldAddi(ASMBasicBlock block) {
        ArrayList<ASMInstruction> insts = new ArrayList<>(block.getInstructions());
        for (int i = 0; i < insts.size() - 1; i++) {
            ASMInstruction inst0 = insts.get(i), inst1 = insts.get(i + 1);
            if (isFoldableAddi(inst0) && isFoldableAddi(inst1) && safelyFold((ASMArithmeticInstruction) inst0, (ASMArithmeticInstruction) inst1)) {
                block.getInstructions().remove(inst0);
                assert inst0.getOperands().get(2) instanceof ASMImmediate && inst1.getOperands().get(2) instanceof ASMImmediate;
                ASMImmediate newImm = new ASMImmediate(((ASMImmediate) inst0.getOperands().get(2)).getImm() + ((ASMImmediate) inst1.getOperands().get(2)).getImm());
                inst1.getOperands().set(2, newImm);
            }
        }
    }

    private void removeRedundantLoadStore(ASMBasicBlock block) {
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

    private void removeUnreachableCode(ASMBasicBlock block) {
        ArrayList<ASMInstruction> instructions = new ArrayList<>(block.getInstructions());
        boolean remove = false;
        for (ASMInstruction inst : instructions) {
            if (remove) {
                inst.removeFromParentBlock();
            } else {
                if (inst.isJump()) remove = true;
            }
        }
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

    @Override
    public void visit(ASMFunction function) {
        this.function = function;
        function.getBlocks().forEach(this::foldMove);
        function.getBlocks().forEach(this::foldAddi);
        function.getBlocks().forEach(this::removeRedundantLoadStore);
        function.getBlocks().forEach(this::convertConstRegisterBranchToJump);
        function.getBlocks().forEach(this::removeUnreachableCode);
        int cnt = 0;
        while (changed && cnt++ < rounds) controlFlowOptimize();
    }
}

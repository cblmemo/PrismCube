package MiddleEnd.ASMOptimize;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Instruction.*;
import ASM.Operand.ASMImmediate;
import ASM.Operand.ASMRegister;
import Memory.Memory;
import MiddleEnd.Pass.ASMFunctionPass;

import java.util.ArrayList;
import java.util.Objects;

import static Debug.MemoLog.log;

/**
 * This class handled all peephole optimize in asm.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class PeepholePeeker implements ASMFunctionPass {
    private boolean changed = false;

    public boolean peek(Memory memory) {
        memory.getAsmModule().getFunctions().values().forEach(this::visit);
        log.Infof("program changed in peephole.\n");
        return changed;
    }

    private boolean safelyFold(ASMMoveInstruction mv0, ASMMoveInstruction mv1) {
        return mv0.getRd() == mv1.getRd() && mv0.getRs() == mv1.getRs();
    }

    // mv reg, reg <- could remove
    private void foldMove(ASMBasicBlock block) {
        ArrayList<ASMInstruction> insts = new ArrayList<>(block.getInstructions());
        for (int i = 0; i < insts.size() - 1; i++) {
            ASMInstruction inst0 = insts.get(i), inst1 = insts.get(i + 1);
            if (inst0 instanceof ASMMoveInstruction && inst1 instanceof ASMMoveInstruction && safelyFold((ASMMoveInstruction) inst0, (ASMMoveInstruction) inst1)) {
                block.getInstructions().remove(inst0);
                changed = true;
            }
        }
    }

    private boolean isFoldableAddi(ASMInstruction inst) {
        return inst.isAddi() && inst.getOperands().get(0) == inst.getOperands().get(1);
    }

    private boolean safelyFold(ASMArithmeticInstruction addi0, ASMArithmeticInstruction addi1) {
        return addi0.getOperands().get(0) == addi1.getOperands().get(0);
    }

    //    addi reg, reg, 1
    //    addi reg, reg, 1
    // -> addi reg, reg, 2
    private void foldAddi(ASMBasicBlock block) {
        ArrayList<ASMInstruction> insts = new ArrayList<>(block.getInstructions());
        for (int i = 0; i < insts.size() - 1; i++) {
            ASMInstruction inst0 = insts.get(i), inst1 = insts.get(i + 1);
            if (isFoldableAddi(inst0) && isFoldableAddi(inst1) && safelyFold((ASMArithmeticInstruction) inst0, (ASMArithmeticInstruction) inst1)) {
                block.getInstructions().remove(inst0);
                assert inst0.getOperands().get(2) instanceof ASMImmediate && inst1.getOperands().get(2) instanceof ASMImmediate;
                ASMImmediate newImm = new ASMImmediate(((ASMImmediate) inst0.getOperands().get(2)).getImm() + ((ASMImmediate) inst1.getOperands().get(2)).getImm());
                inst1.getOperands().set(2, newImm);
                changed = true;
            }
        }
    }

    // load  reg, addr
    // store reg, addr <- could remove
    private void removeRedundantLoadStore(ASMBasicBlock block) {
        for (int i = 1; i < block.getInstructions().size(); i++) {
            ASMInstruction pre = block.getInstructions().get(i - 1), now = block.getInstructions().get(i);
            if (pre instanceof ASMMemoryInstruction && now instanceof ASMMemoryInstruction) {
                if (!pre.isStore() && now.isStore()) {
                    if (pre.getOperands().get(0) == now.getOperands().get(0) && Objects.equals(pre.getOperands().get(1), now.getOperands().get(1))) {
                        i--;
                        now.removeFromParentBlock();
                        changed = true;
                    }
                }
            }
        }
    }

    //    seqz reg, src
    //    beqz reg, .LABEL
    // -> bnez src, .LABEL
    // NOTICE: reg need to be overwrote at all successors before its use
    private void convertBranch(ASMBasicBlock block) {
        for (int i = 0; i < block.getInstructions().size() - 1; i++) {
            ASMInstruction inst0 = block.getInstructions().get(i), inst1 = block.getInstructions().get(i + 1);
            if (inst0.isSeqz() && inst1.isBeqz() && inst0.getOperands().get(0) == inst1.getOperands().get(0) && inst0.getOperands().get(0) != inst0.getOperands().get(1)) {
                assert inst0.getOperands().get(0) instanceof ASMRegister;
                for (ASMBasicBlock succ : block.getSuccessors()) {
                    if (!succ.definedBeforeUse((ASMRegister) inst0.getOperands().get(0))) return;
                }
                block.getInstructions().remove(inst0);
                block.getInstructions().remove(inst1);
                ASMPseudoInstruction bnez = new ASMPseudoInstruction(block, ASMPseudoInstruction.InstType.bnez);
                block.getInstructions().add(i, bnez.addOperand(inst0.getOperands().get(1)).addOperand(inst1.getOperands().get(1)));
                changed = true;
            }
        }
    }

    private boolean isValidImm(int imm) {
        return -2048 <= imm && imm <= 2047;
    }

    //    li   reg0, 100
    //    add  reg0, reg1, reg0
    // -> addi reg0, reg1, 100
    private void mergeLiAndArith(ASMBasicBlock block) {
        for (int i = 0; i < block.getInstructions().size() - 1; i++) {
            ASMInstruction inst0 = block.getInstructions().get(i), inst1 = block.getInstructions().get(i + 1);
            if (inst0.isLi() && inst1.haveImmediateType()) {
                assert inst0.getOperands().get(1) instanceof ASMImmediate;
                if (!isValidImm(((ASMImmediate) inst0.getOperands().get(1)).getImm())) continue;
                if (inst0.getOperands().get(0) == inst1.getOperands().get(0) && inst1.getOperands().get(0) == inst1.getOperands().get(2)) {
                    if (inst1.getOperands().get(0) != inst1.getOperands().get(1)) {
                        block.getInstructions().remove(inst0);
                        block.getInstructions().remove(inst1);
                        assert inst1 instanceof ASMArithmeticInstruction;
                        ASMArithmeticInstruction immInst = new ASMArithmeticInstruction(block, ((ASMArithmeticInstruction) inst1).getImmediateType());
                        block.getInstructions().add(i, immInst.addOperand(inst0.getOperands().get(0)).addOperand(inst1.getOperands().get(1)).addOperand(inst0.getOperands().get(1)));
                        changed = true;
                    }
                }
            }
        }
    }

    @Override
    public void visit(ASMFunction function) {
        function.getBlocks().forEach(this::foldMove);
        function.getBlocks().forEach(this::foldAddi);
        function.getBlocks().forEach(this::removeRedundantLoadStore);
        function.getBlocks().forEach(this::convertBranch);
        function.getBlocks().forEach(this::mergeLiAndArith);
    }
}

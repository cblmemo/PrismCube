package MiddleEnd.ASMOptimize;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Instruction.*;
import ASM.Operand.ASMImmediate;
import Memory.Memory;
import MiddleEnd.Pass.ASMFunctionPass;

import java.util.ArrayList;
import java.util.Objects;

public class PeepholePeeker implements ASMFunctionPass {
    private boolean changed = false;

    public boolean peek(Memory memory) {
        memory.getAsmModule().getFunctions().values().forEach(this::visit);
        return changed;
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

    @Override
    public void visit(ASMFunction function) {
        function.getBlocks().forEach(this::foldMove);
        function.getBlocks().forEach(this::foldAddi);
        function.getBlocks().forEach(this::removeRedundantLoadStore);
    }
}

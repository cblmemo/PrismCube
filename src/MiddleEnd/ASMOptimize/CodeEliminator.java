package MiddleEnd.ASMOptimize;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Instruction.ASMInstruction;
import Memory.Memory;
import MiddleEnd.Pass.ASMFunctionPass;

import java.util.ArrayList;

import static Debug.MemoLog.log;

public class CodeEliminator implements ASMFunctionPass {
    private boolean changed = false;

    public boolean eliminate(Memory memory) {
        memory.getAsmModule().getFunctions().values().forEach(this::visit);
        log.Infof("program changed in code eliminate.\n");
        return changed;
    }

    private void removeUnreachableCode(ASMBasicBlock block) {
        ArrayList<ASMInstruction> instructions = new ArrayList<>(block.getInstructions());
        boolean remove = false;
        for (ASMInstruction inst : instructions) {
            if (remove) {
                inst.removeFromParentBlock();
                changed = true;
            } else {
                if (inst.isJump()) remove = true;
            }
        }
    }

    private void removeUselessCode(ASMBasicBlock block) {
        ArrayList<ASMInstruction> instructions = new ArrayList<>(block.getInstructions());
        instructions.forEach(inst -> {
            if (inst.useless()) {
                block.getInstructions().remove(inst);
                changed = true;
            }
        });
    }

    @Override
    public void visit(ASMFunction function) {
        function.getBlocks().forEach(this::removeUnreachableCode);
        function.getBlocks().forEach(this::removeUselessCode);
    }
}

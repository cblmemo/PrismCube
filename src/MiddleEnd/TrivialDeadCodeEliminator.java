package MiddleEnd;

import IR.IRFunction;
import IR.Instruction.IRInstruction;
import Memory.Memory;

import java.util.ArrayList;

public class TrivialDeadCodeEliminator extends IROptimize {
    public void eliminate(Memory memory) {
        if (doOptimize) {
            memory.getIRModule().getFunctions().values().forEach(this::visit);
        }
    }

    @Override
    protected void visit(IRFunction function) {
        function.getBlocks().forEach(block -> {
            ArrayList<IRInstruction> instructions = new ArrayList<>(block.getInstructions());
            instructions.forEach(inst -> {
                if (inst.noUsersAndSafeToRemove()) inst.removeFromParentBlock();
            });
        });
    }
}

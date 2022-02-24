package MiddleEnd;

import IR.IRFunction;
import IR.Instruction.IRInstruction;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;

import java.util.ArrayList;

public class TrivialDeadCodeEliminator implements IRFunctionPass {
    public void eliminate(Memory memory) {
        memory.getIRModule().getFunctions().values().forEach(this::visit);
    }

    @Override
    public void visit(IRFunction function) {
        function.getBlocks().forEach(block -> {
            ArrayList<IRInstruction> instructions = new ArrayList<>(block.getInstructions());
            instructions.forEach(inst -> {
                if (inst.noUsersAndSafeToRemove()) inst.removeFromParentBlock();
            });
        });
    }
}

package MiddleEnd;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.Instruction.IRJumpInstruction;
import IR.Instruction.IRMoveInstruction;
import IR.Instruction.IRPhiInstruction;
import IR.Operand.IRRegister;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class PhiResolver implements IRFunctionPass {
    private IRFunction function;

    public void resolve(Memory memory) {
        memory.getIRModule().getFunctions().values().forEach(this::visit);
    }

    private void criticalEdgeSplit() {
        LinkedHashSet<IRBasicBlock> newBlocks = new LinkedHashSet<>();
        function.getBlocks().forEach(pred -> {
            if (pred.getSuccessors().size() <= 1) return;
            ArrayList<IRBasicBlock> successors = new ArrayList<>(pred.getSuccessors());
            successors.forEach(succ -> {
                if (succ.getPredecessors().size() <= 1) return;
                // now pred -> succ is a critical edge
                IRBasicBlock mid = new IRBasicBlock(function, "middle" + newBlocks.size());
                mid.setEscapeInstruction(new IRJumpInstruction(mid, succ));
                mid.finishBlock();
                newBlocks.add(mid);
                succ.getPhis().forEach(phi -> phi.replaceSourceBlock(pred, mid));
                pred.replaceControlFlowTarget(succ, mid);
                succ.replacePredecessor(pred, mid);
            });
        });
        function.addAllNewBlocks(newBlocks);
    }

    private void replacePhiWithMove() {
        function.getBlocks().forEach(block -> {
            ArrayList<IRPhiInstruction> phis = new ArrayList<>(block.getPhis());
            phis.forEach(phi -> {
                IRRegister phiResult = phi.getResultRegister();
                phi.forEachCandidate((src, val) -> src.insertInstructionBeforeEscape(new IRMoveInstruction(src, phiResult, val)));
                phi.removeFromParentBlock();
            });
        });
    }

    @Override
    public void visit(IRFunction function) {
        this.function = function;
        criticalEdgeSplit();
        replacePhiWithMove();
    }
}

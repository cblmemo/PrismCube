package MiddleEnd.IROptimize;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.Instruction.IRJumpInstruction;
import IR.Instruction.IRMoveInstruction;
import IR.Instruction.IRPhiInstruction;
import IR.Operand.IRRegister;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;
import MiddleEnd.Utils.CopyInterfereGraph;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * This class eliminate dead code using an aggressive strategy.
 * <br>Algorithm: Tiger Book, Chapter 19.3
 *
 * @author rainy memory
 * @version 1.0.0
 */

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

    /**
     * Naive implementations for phi resolve.
     * <br>This implementation will encounter bugs when
     * <pre> {@code
     *      %0 = phi [%1, B1], [v2, B2]
     *      %1 = phi [%0, B1], [V2, B2]
     * } </pre>
     * This implementation will produce
     * <pre> {@code
     *      %0 = move %1
     *      %1 = move %0
     * } </pre>
     * in B1, this overlapping results in bugs.
     *
     * @deprecated
     */
    private void naiveImplementation() {
        function.getBlocks().forEach(block -> {
            ArrayList<IRPhiInstruction> phis = new ArrayList<>(block.getPhis());
            phis.forEach(phi -> {
                IRRegister phiResult = phi.getResultRegister();
                phi.forEachCandidate((src, val) -> {
                    assert src.getSuccessors().contains(block) : src.getSuccessors() + " " + block;
                    assert block.getPredecessors().contains(src) : block.getPredecessors() + " " + src;
                    src.insertInstructionBeforeEscape(new IRMoveInstruction(src, phiResult, val));
                });
                phi.removeFromParentBlock();
            });
        });
    }

    private void buildGopyInterfereGraph() {
        function.getBlocks().forEach(block -> block.getPhis().forEach(phi -> phi.forEachCandidate((src, val) -> src.getGraph().addEdge(phi, phi.getResultRegister(), val))));
    }

    private void replacePhiWithMove(IRBasicBlock block) {
        CopyInterfereGraph graph = block.getGraph();
        boolean changed = true;
        while (changed) {
            changed = false;
            LinkedHashSet<CopyInterfereGraph.Edge> edges = new LinkedHashSet<>(graph.getEdges());
            for (CopyInterfereGraph.Edge edge : edges) {
                if (graph.isFree(edge)) {
                    block.insertInstructionBeforeEscape(new IRMoveInstruction(block, edge.getRd(), edge.getRs()));
                    graph.eraseEdge(edge);
                    edge.getParentPhi().removeCandidate(block);
                    changed = true;
                }
            }
        }
    }

    private void applyParallelMove(IRBasicBlock block) {
        CopyInterfereGraph graph = block.getGraph();
        boolean changed = true;
        while (changed) {
            changed = false;
            replacePhiWithMove(block);
            LinkedHashSet<CopyInterfereGraph.Edge> edges = new LinkedHashSet<>(graph.getEdges());
            for (CopyInterfereGraph.Edge edge : edges) {
                if (edge.getRd() == edge.getRs()) continue;
                IRRegister alias = new IRRegister(edge.getRs().getIRType(), "rs_alias");
                block.insertInstructionBeforeEscape(new IRMoveInstruction(block, alias, edge.getRs()));
                graph.eraseEdge(edge);
                graph.addEdge(edge.getParentPhi(), edge.getRd(), alias);
                changed = true;
                break;
            }
        }
    }

    private void removeRedundantPhi() {
        function.getBlocks().forEach(block -> {
            ArrayList<IRPhiInstruction> phis = new ArrayList<>(block.getPhis());
            phis.forEach(phi -> {
                if (phi.getBlocks().isEmpty()) phi.removeFromParentBlock();
            });
        });
    }

    @Override
    public void visit(IRFunction function) {
        this.function = function;
        criticalEdgeSplit();
        buildGopyInterfereGraph();
        function.getBlocks().forEach(this::applyParallelMove);
        removeRedundantPhi();
    }
}

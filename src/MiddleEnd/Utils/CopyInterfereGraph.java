package MiddleEnd.Utils;

import IR.Instruction.IRPhiInstruction;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * This class store copy interfere graph for PhiResolver.
 *
 * @see MiddleEnd.IROptimize.PhiResolver
 * @author rainy memory
 * @version 1.0.0
 */

public class CopyInterfereGraph {
    public static class Edge {
        private final IRPhiInstruction parentPhi;
        private final IRRegister rd;
        private final IROperand rs;

        Edge(IRPhiInstruction parentPhi, IRRegister rd, IROperand rs) {
            this.parentPhi = parentPhi;
            this.rd = rd;
            this.rs = rs;
        }

        public IRPhiInstruction getParentPhi() {
            return parentPhi;
        }

        public IRRegister getRd() {
            return rd;
        }

        public IROperand getRs() {
            return rs;
        }
    }

    private final LinkedHashSet<Edge> edges = new LinkedHashSet<>();
    private final LinkedHashMap<IROperand, Integer> usedBy = new LinkedHashMap<>();

    public LinkedHashSet<Edge> getEdges() {
        return edges;
    }

    public void addEdge(IRPhiInstruction parentPhi, IRRegister rd, IROperand rs) {
        edges.add(new Edge(parentPhi, rd, rs));
        usedBy.put(rs, usedBy.getOrDefault(rs, 0) + 1);
    }

    public void eraseEdge(Edge edge) {
        edges.remove(edge);
        assert usedBy.containsKey(edge.getRs()) && usedBy.get(edge.getRs()) > 0;
        usedBy.put(edge.getRs(), usedBy.get(edge.getRs()) - 1);
    }

    public boolean isFree(Edge edge) {
        return usedBy.getOrDefault(edge.getRd(), 0) == 0;
    }
}

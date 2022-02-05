package MiddleEnd;

import IR.IRBasicBlock;
import IR.IRFunction;
import Memory.Memory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class DominatorTreeBuilder extends Optimize {
    private IRFunction function;

    // auxiliary
    private final LinkedHashMap<IRBasicBlock, IRBasicBlock> idom = new LinkedHashMap<>();
    private final LinkedHashMap<IRBasicBlock, IRBasicBlock> semi = new LinkedHashMap<>();
    private final LinkedHashMap<IRBasicBlock, Integer> dfn = new LinkedHashMap<>();
    private final LinkedHashMap<IRBasicBlock, IRBasicBlock> dfsFather = new LinkedHashMap<>();
    private final ArrayList<IRBasicBlock> order = new ArrayList<>();

    // disjoint set
    private final LinkedHashMap<IRBasicBlock, IRBasicBlock> fa = new LinkedHashMap<>();
    private final LinkedHashMap<IRBasicBlock, IRBasicBlock> mn = new LinkedHashMap<>();

    // dominator tree
    private final LinkedHashMap<IRBasicBlock, LinkedHashSet<IRBasicBlock>> semiDominatorTree = new LinkedHashMap<>();
    private final LinkedHashMap<IRBasicBlock, LinkedHashSet<IRBasicBlock>> dominatorFrontier = new LinkedHashMap<>();

    public void build(Memory memory) {
        if (doOptimize) {
            memory.getIRModule().getFunctions().values().forEach(this::visit);
        }
    }

    private void initialize() {
        idom.clear();
        semi.clear();
        dfn.clear();
        dfsFather.clear();
        order.clear();
        fa.clear();
        mn.clear();
        semiDominatorTree.clear();
        dominatorFrontier.clear();
        ArrayList<IRBasicBlock> blocks = function.reachableBlocks();
        blocks.forEach(block -> {
            idom.put(block, null);
            semi.put(block, block);
            fa.put(block, block);
            mn.put(block, block);
            semiDominatorTree.put(block, new LinkedHashSet<>());
            dominatorFrontier.put(block, new LinkedHashSet<>());
        });
    }

    private void dfsOrder(IRBasicBlock current) {
        dfn.put(current, order.size());
        order.add(current);
        current.getSuccessors().forEach(succ -> {
            if (!order.contains(succ)) {
                dfsFather.put(succ, current);
                dfsOrder(succ);
            }
        });
    }

    private IRBasicBlock find(IRBasicBlock k) {
        if (k == fa.get(k)) return k;
        IRBasicBlock res = find(fa.get(k));
        if (dfn.get(semi.get(mn.get(fa.get(k)))) < dfn.get(semi.get(mn.get(k)))) mn.replace(k, mn.get(fa.get(k)));
        fa.replace(k, res);
        return res;
    }

    private void buildDominatorTree() {
        for (int i = order.size() - 1; i > 0; i--) {
            IRBasicBlock t = order.get(i);
            for (IRBasicBlock y : t.getPredecessors()) {
                if (!dfn.containsKey(y)) return;
                find(y);
                if (dfn.get(semi.get(mn.get(y))) < dfn.get(semi.get(t))) semi.replace(t, semi.get(mn.get(y)));
            }
            fa.replace(t, dfsFather.get(t));
            semiDominatorTree.get(semi.get(t)).add(t);
            t = dfsFather.get(t);
            for (IRBasicBlock y : semiDominatorTree.get(t)) {
                find(y);
                idom.replace(y, t == semi.get(mn.get(y)) ? t : mn.get(y));
            }
            semiDominatorTree.get(t).clear();
        }
        for (int i = 1; i < order.size(); i++) {
            IRBasicBlock t = order.get(i);
            if (idom.get(t) != semi.get(t)) idom.replace(t, idom.get(idom.get(t)));
        }
    }

    private void calculateDominatorFrontier() {
        order.forEach(b -> {
            if (b.getPredecessors().size() >= 2) {
                b.getPredecessors().forEach(p -> {
                    IRBasicBlock runner = p;
                    while (runner != idom.get(b)) {
                        dominatorFrontier.get(runner).add(b);
                        runner = idom.get(runner);
                    }
                });
            }
        });
    }

    @Override
    protected void visit(IRFunction function) {
        this.function = function;
        initialize();
        dfsOrder(function.getEntryBlock());
        buildDominatorTree();
        calculateDominatorFrontier();
    }
}

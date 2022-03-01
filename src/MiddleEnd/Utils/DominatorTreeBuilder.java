package MiddleEnd.Utils;

import IR.IRBasicBlock;
import IR.IRFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import static Debug.MemoLog.log;

/**
 * This class build dominator tree for graph of IRBasicBlock.
 *
 * @see MiddleEnd.IROptimize.MemoryToRegisterPromoter
 * @see MiddleEnd.IROptimize.AggressiveDeadCodeEliminator
 * @see LoopExtractor
 * @author rainy memory
 * @version 1.0.0
 */

public class DominatorTreeBuilder {
    private IRFunction function;
    private boolean reverse;
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

    private void initialize() {
        ArrayList<IRBasicBlock> blocks = function.reachableBlocks();
        log.Tracef("reachable blocks: %s\n", blocks);
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
        (reverse ? current.getPredecessors() : current.getSuccessors()).forEach(succ -> {
            if (!dfn.containsKey(succ)) {
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
            for (IRBasicBlock y : (reverse ? t.getSuccessors() : t.getPredecessors())) {
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
        // store to block
        if (reverse) idom.forEach(IRBasicBlock::setPostIdom);
        else {
            idom.forEach(IRBasicBlock::setIdom);
            idom.forEach((succ, pred) -> {
                if (pred != null) {
                    pred.addDominatorTreeSuccessor(succ);
                    log.Tracef("%s dom %s\n", pred, succ);
                } else log.Tracef("%s has no idom\n", succ);
            });
        }
    }

    private void calculateDominatorFrontier() {
        order.forEach(b -> {
            if ((reverse ? b.getSuccessors() : b.getPredecessors()).size() >= 2) {
                (reverse ? b.getSuccessors() : b.getPredecessors()).forEach(p -> {
                    IRBasicBlock runner = p;
                    while (runner != idom.get(b)) {
                        assert dominatorFrontier.containsKey(runner) : runner + " is not in dominatorFrontier";
                        dominatorFrontier.get(runner).add(b);
                        runner = idom.get(runner);
                    }
                });
            }
        });
        // store to function
        if (reverse) function.setPostDominatorFrontier(dominatorFrontier);
        else function.setDominatorFrontier(dominatorFrontier);
        log.Tracef("%sDominatorFrontier: %s\n", reverse ? "reverse " : "", dominatorFrontier);
    }

    public void build(IRFunction function, boolean reverse) {
        this.function = function;
        this.reverse = reverse;
        function.removeUnreachableBlocks();
        initialize();
        dfsOrder(reverse ? function.getReturnBlock() : function.getEntryBlock());
        buildDominatorTree();
        calculateDominatorFrontier();
    }
}

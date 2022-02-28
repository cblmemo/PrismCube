package MiddleEnd.Utils;

import IR.IRBasicBlock;
import IR.IRFunction;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import static Debug.MemoLog.log;

public class LoopExtractor {
    private IRFunction function;
    // header -> terminal
    private final LinkedHashMap<IRBasicBlock, IRBasicBlock> backEdge = new LinkedHashMap<>();
    // header -> loop
    private final LinkedHashMap<IRBasicBlock, IRLoop> loops = new LinkedHashMap<>();

    private void findBackEdge() {
        function.getBlocks().forEach(block -> block.getSuccessors().forEach(succ -> {
            if (block.dominatedBy(succ)) backEdge.put(succ, block);
        }));
    }

    private void findNaturalLoop(IRBasicBlock header, IRBasicBlock terminal) {
        if (!loops.containsKey(header)) loops.put(header, new IRLoop(header));
        IRLoop loop = loops.get(header);
        loop.addTerminal(terminal);
        LinkedHashSet<IRBasicBlock> visited = new LinkedHashSet<>();
        Queue<IRBasicBlock> queue = new LinkedList<>();
        queue.offer(terminal);
        while (!queue.isEmpty()) {
            IRBasicBlock cur = queue.poll();
            if (visited.contains(cur) || cur == header) continue;
            visited.add(cur);
            loop.addNode(cur);
            cur.getPredecessors().forEach(pred -> {
                if (pred.dominatedBy(header)) queue.offer(pred);
            });
        }
    }

    private boolean tryAgain() {
        AtomicBoolean changed = new AtomicBoolean(false);
        loops.forEach((header, loop) -> loop.forEachNonHeaderNode(node -> {
            if (loops.containsKey(node)) changed.set(changed.get() | loop.addInnerLoop(loops.get(node)));
        }));
        return changed.get();
    }

    private void constructLoopNestTree() {
        function.getTopLoops().clear();
        while (true) if (!tryAgain()) break;
        loops.values().forEach(loop -> {
            if (loop.getParentLoop() == null) function.addTopLoop(loop);
        });
    }

    private void setDepth(IRLoop cur, int depth) {
        cur.setDepth(depth);
        cur.forEachInnerLoop(loop -> setDepth(loop, depth + 1));
    }

    private void print(IRLoop cur, int depth) {
        log.Tracef("depth: %d, loop: %s\n", depth, cur);
        cur.forEachInnerLoop(loop -> print(loop, depth + 1));
    }

    public void extract(IRFunction function) {
        this.function = function;
        new DominatorTreeBuilder().build(function, false);
        findBackEdge();
        backEdge.forEach(this::findNaturalLoop);
        constructLoopNestTree();
        function.getTopLoops().forEach(topLoop -> {
            setDepth(topLoop, 0);
            topLoop.simplifyLoopNestTree();
            log.Tracef(" -------- start print loop -------- \n");
            print(topLoop, 0);
            log.Tracef(" -------- print loop end -------- \n");
        });
    }
}

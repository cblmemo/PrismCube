package MiddleEnd;

import FrontEnd.IREmitter;
import Memory.Memory;

import static Debug.MemoLog.log;

public class IROptimizer extends Optimize {
    static private final int rounds = 30;

    public void invoke(Memory memory) {
        if (level == OptimizeLevel.O0) return;
        new IREmitter().emitDebug(memory, "./bin/opt-mem2reg-before.ll");
        new MemoryToRegisterPromoter().promote(memory);
        int cnt = 0;
        boolean changed;
        while (cnt++ < rounds) {
            log.Infof("Optimize round %d\n", cnt);
            changed = false;

            new IREmitter().emitDebug(memory, "./bin/opt-adce-before-" + cnt + ".ll");
            changed |= new AggressiveDeadCodeEliminator().eliminate(memory);
            new ControlFlowGraphChecker("after adce in round " + cnt).check(memory);

            new IREmitter().emitDebug(memory, "./bin/opt-sccp-before-" + cnt + ".ll");
            changed |= new SparseConditionalConstantPropagator().propagate(memory);
            new ControlFlowGraphChecker("after sccp in round " + cnt).check(memory);

            new IREmitter().emitDebug(memory, "./bin/opt-inline-before-" + cnt + ".ll");
            changed |= new FunctionInliner().inline(memory);
            new ControlFlowGraphChecker("after inline in round " + cnt).check(memory);

            new IREmitter().emitDebug(memory, "./bin/opt-fuse-before-" + cnt + ".ll");
            changed |= new IRBlockFuser().fuse(memory);
            new ControlFlowGraphChecker("after fuse in round " + cnt).check(memory);

            new IREmitter().emitDebug(memory, "./bin/opt-optimize-after-" + cnt + ".ll");
            if (!changed) break;
        }
        new PhiResolver().resolve(memory);
        // following pass' function was covered by FunctionInliner
        // new IRGlobalInitializeEliminator().eliminate(memory);
        new IREmitter().emitOpt(memory);
    }
}

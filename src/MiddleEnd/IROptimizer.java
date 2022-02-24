package MiddleEnd;

import FrontEnd.IREmitter;
import Memory.Memory;

import static Debug.MemoLog.log;

public class IROptimizer extends Optimize {
    public void invoke(Memory memory) {
        if (level == OptimizeLevel.O0) return;
        new IREmitter().emitToFile(memory, "./bin/opt-mem2reg-before.ll");
        new MemoryToRegisterPromoter().promote(memory);
        int cnt = 0;
        boolean changed;
        while (cnt++ < 10) {
            log.Infof("Optimize round %d\n", cnt);
            changed = false;
            new IREmitter().emitToFile(memory, "./bin/opt-adce-before-" + cnt + ".ll");
            changed |= new AggressiveDeadCodeEliminator().eliminate(memory);
            new ControlFlowGraphChecker("after adce in round " + cnt).check(memory);
            new IREmitter().emitToFile(memory, "./bin/opt-sccp-before-" + cnt + ".ll");
            changed |= new SparseConditionalConstantPropagator().propagate(memory);
            new ControlFlowGraphChecker("after sccp in round " + cnt).check(memory);
            new IREmitter().emitToFile(memory, "./bin/opt-fuse-before-" + cnt + ".ll");
            changed |= new IRBlockFuser().fuse(memory);
            new ControlFlowGraphChecker("after fuse in round " + cnt).check(memory);
            new IREmitter().emitToFile(memory, "./bin/opt-optimize-after-" + cnt + ".ll");
            if (!changed) break;
        }
        new PhiResolver().resolve(memory);
        new IRGlobalInitializeEliminator().eliminate(memory);
        new IREmitter().emitOpt(memory);
    }
}

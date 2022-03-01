package MiddleEnd;

import Memory.Memory;
import MiddleEnd.ASMOptimize.*;

import static Debug.MemoLog.log;

public class ASMOptimizer extends Optimize {
    static private final int rounds = 30;

    public void invoke(Memory memory) {
        if (level == OptimizeLevel.O0) return;

        int cnt = 0;
        boolean changed = true;
        while (cnt++ < rounds) {
            log.Infof("ASM Optimize round %d\n", cnt);

            changed |= new PeepholePeeker().peek(memory);

            changed |= new CodeEliminator().eliminate(memory);

            if (!changed) {
                log.Infof("No changed has made in this turn.\n");
                break;
            }

            changed = false;

            if (level == OptimizeLevel.O1) continue;

            changed |= new ControlFlowSimplifyer().simplify(memory);
        }

        if (level == OptimizeLevel.O3) new CodePuller().pull(memory);

        new BlockReorderer().reorder(memory);
    }
}

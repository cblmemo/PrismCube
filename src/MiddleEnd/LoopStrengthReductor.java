package MiddleEnd;

import IR.IRFunction;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;
import MiddleEnd.Utils.LoopExtractor;

import static Debug.MemoLog.log;

public class LoopStrengthReductor implements IRFunctionPass {
    private boolean changed = false;

    public boolean reduce(Memory memory) {
        memory.getIRModule().getFunctions().values().forEach(this::visit);
        if (changed) log.Infof("Program changed in loop.\n");
        return changed;
    }

    @Override
    public void visit(IRFunction function) {
        new LoopExtractor().extract(function);
    }
}

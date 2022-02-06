package MiddleEnd;

import IR.IRFunction;
import Memory.Memory;

public class PhiResolver extends IROptimize {
    public void resolve(Memory memory) {
        if (doOptimize) {
            memory.getIRModule().getFunctions().values().forEach(this::visit);
        }
    }

    @Override
    protected void visit(IRFunction function) {

    }
}

package Utility.Scope;

import IR.IRBasicBlock;

public class LoopScope extends BlockScope {
    // for ir
    private IRBasicBlock loopConditionBlock;
    private IRBasicBlock loopTerminateBlock;

    public LoopScope(Scope parentScope) {
        super(parentScope);
    }

    // for ir
    public void setLoopConditionBlock(IRBasicBlock loopConditionBlock) {
        this.loopConditionBlock = loopConditionBlock;
    }

    public IRBasicBlock getLoopConditionBlock() {
        return loopConditionBlock;
    }

    public void setLoopTerminateBlock(IRBasicBlock loopTerminateBlock) {
        this.loopTerminateBlock = loopTerminateBlock;
    }

    public IRBasicBlock getLoopTerminateBlock() {
        return loopTerminateBlock;
    }
}

package Utility.Scope;

import IR.IRBasicBlock;

public class LoopScope extends BlockScope {
    // for ir
    private IRBasicBlock loopConditionBlock = null;
    private IRBasicBlock loopStepBlock = null;
    private IRBasicBlock loopTerminateBlock = null;

    public LoopScope(Scope parentScope) {
        super(parentScope);
    }

    // for ir
    public void setLoopConditionBlock(IRBasicBlock loopConditionBlock) {
        this.loopConditionBlock = loopConditionBlock;
    }

    public IRBasicBlock getContinueTarget() {
        return loopStepBlock == null ? loopConditionBlock : loopStepBlock;
    }

    public void setLoopStepBlock(IRBasicBlock loopStepBlock) {
        this.loopStepBlock = loopStepBlock;
    }

    public IRBasicBlock getLoopStepBlock() {
        return loopStepBlock;
    }

    public void setLoopTerminateBlock(IRBasicBlock loopTerminateBlock) {
        this.loopTerminateBlock = loopTerminateBlock;
    }

    public IRBasicBlock getLoopTerminateBlock() {
        return loopTerminateBlock;
    }
}

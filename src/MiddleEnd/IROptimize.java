package MiddleEnd;

import IR.IRFunction;

abstract public class IROptimize extends Optimize {
    abstract protected void visit(IRFunction function);
}

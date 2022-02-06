package MiddleEnd;

import ASM.ASMFunction;

abstract public class ASMOptimize extends Optimize {
    abstract protected void visit(ASMFunction function);
}

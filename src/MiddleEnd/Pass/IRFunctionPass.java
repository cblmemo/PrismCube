package MiddleEnd.Pass;

import IR.IRFunction;

public interface IRFunctionPass {
    void visit(IRFunction function);
}

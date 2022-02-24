package MiddleEnd.Pass;

import ASM.ASMFunction;

public interface ASMFunctionPass {
    void visit(ASMFunction function);
}

package MiddleEnd;

import IR.IRFunction;

public abstract class Optimize {
    protected static boolean doOptimize = false;

    public static void enable() {
        doOptimize = true;
    }

    public static void disable() {
        doOptimize = false;
    }

    abstract protected void visit(IRFunction function);
}

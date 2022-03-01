package MiddleEnd;

public abstract class Optimize {
    public enum OptimizeLevel {
        O0, O1, O2, O3
    }

    protected static OptimizeLevel level = OptimizeLevel.O0;

    public static void setLevel(OptimizeLevel level) {
        Optimize.level = level;
    }
}

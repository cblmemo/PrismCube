package MiddleEnd;

public abstract class Optimize {
    protected static boolean doOptimize = false;

    public static void enable() {
        doOptimize = true;
    }

    public static void disable() {
        doOptimize = false;
    }

    // todo replace flag with this:
    //  class OptimizationManager {
    //  public Array<Optimization> getOptimizations (String level) {
    //    if (level.equals("-O0")) return [];
    //    if (level.equals("-O1")) return [
    //      new FooOptimization(),
    //      new BarOptimization(/* level */ 1)
    //    ];
    //    // ...
    //  }
    //}
}

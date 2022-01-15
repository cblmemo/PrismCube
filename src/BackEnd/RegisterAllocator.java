package BackEnd;

import ASM.RegisterAllocate.GraphColoringAllocator;
import ASM.RegisterAllocate.NaiveAllocator;
import Memory.Memory;

/**
 * This class allocate virtual register to physical
 * register.
 * In naive implementation, we directly spill all
 * virtual register to stack. For more detail,
 * @see NaiveAllocator
 * In graph coloring implementation,
 * to be done...
 * For more detail,
 * @see GraphColoringAllocator
 *
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class RegisterAllocator {
    private static boolean allocate = false;
    private static final boolean naive = true;

    public static void enable() {
        allocate = true;
    }

    public static void disable() {
        allocate = false;
    }

    public static boolean naive() {
        return naive;
    }

    public void allocate(Memory memory) {
        if (allocate) memory.getAsmModule().getFunctions().values().forEach(function -> {
            if (naive) new NaiveAllocator(function).allocate();
            else new GraphColoringAllocator(function).allocate();
        });
    }
}

package BackEnd;

import ASM.RegisterAllocate.GraphColoringAllocator;
import ASM.RegisterAllocate.NaiveAllocator;
import Memory.Memory;

public class RegisterAllocator {
    private static boolean allocate = false;
    private static boolean naive = true;

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

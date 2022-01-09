package BackEnd;

import ASM.RegisterAllocate.GraphColoringAllocator;
import ASM.RegisterAllocate.NaiveAllocator;
import Memory.Memory;

public class RegisterAllocator {
    static boolean allocate = false;
    static boolean naive = true;

    public static void enable() {
        allocate = true;
    }

    public static void disable() {
        allocate = false;
    }

    public void naiveAllocate(Memory memory) {
        if (allocate) memory.getAsmModule().getFunctions().values().forEach(function -> {
            if (naive) new NaiveAllocator(function).allocate();
            else new GraphColoringAllocator(function).allocate();
        });
    }
}

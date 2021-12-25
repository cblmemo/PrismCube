package BackEnd;

import Memory.Memory;

import java.io.PrintStream;

public class ASMPrinter {
    private PrintStream ps;

    static boolean print = false;

    public static void enable() {
        print = true;
    }

    public static void disable() {
        print = false;
    }

    /**
     * This method print RISCV asm to PrintStream
     * specified by Memory.
     *
     * @see Memory
     */
    public void print(Memory memory) {
        if (print) {
            ps = memory.getPrintStream();

        }
    }

}

package BackEnd;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.ASMModule;
import ASM.Instruction.ASMInstruction;
import Memory.Memory;

import java.io.PrintStream;

public class ASMPrinter {
    private PrintStream ps;
    private int indentCnt = 0;
    private int functionCnt = 0;

    private static boolean print = false;
    private static boolean printVirtual = false;
    private static final int commentAlignLength = 50;
    private static final int pseudoOptionsAlignLength = 10;

    public static void enable() {
        print = true;
    }

    public static void disable() {
        print = false;
    }

    public static void enableVirtual() {
        printVirtual = true;
    }

    public static void disableVirtual() {
        printVirtual = false;
    }

    private void printWithIndent(String str) {
        for (int i = 0; i < indentCnt; i++) ps.print("\t");
        ps.println(str);
    }

    private String formatComment(String comment, String str) {
        int spaceNum = commentAlignLength - str.length() - 4 * indentCnt;
        assert spaceNum > 0;
        return str + " ".repeat(spaceNum) + "#" + comment;
    }

    private String formatPseudoOptions(String option, String value) {
        int spaceNum = pseudoOptionsAlignLength - option.length() - 1;
        assert spaceNum > 0;
        return "." + option + " ".repeat(spaceNum) + value;
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
            print(memory.getAsmModule());
        }
    }

    public void printVirtual(Memory memory) {
        if (printVirtual) {
            ps = memory.getDebugStream();
            print(memory.getAsmModule());
        }
    }

    private void print(ASMModule module) {
        indentCnt++;
        printWithIndent(".text");
        printWithIndent(formatPseudoOptions("file", "\"src.mx\""));
        indentCnt--;
        module.getFunctions().values().forEach(this::print);
        // todo print const string and global variable
    }

    private void print(ASMFunction function) {
        String funcName = function.getFunctionName();
        indentCnt++;
        printWithIndent(formatComment(" -- Begin Function " + funcName, formatPseudoOptions("globl", funcName)));
        printWithIndent(formatPseudoOptions("p2align", "2"));
        printWithIndent(formatPseudoOptions("type", funcName + ",@function"));
        indentCnt--;
        printWithIndent(formatComment(" @" + funcName, funcName + ":"));
        function.getBlocks().forEach(this::print);
        String endLabel = ".Lfunc_end" + (functionCnt++);
        printWithIndent(endLabel + ":");
        indentCnt++;
        printWithIndent(formatPseudoOptions("size", funcName + ", " + endLabel + "-" + funcName));
        indentCnt--;
        printWithIndent(formatComment(" -- End Function", ""));
    }

    private void print(ASMBasicBlock block) {
        printWithIndent(block.getLabel() + ":");
        indentCnt++;
        block.getInstructions().forEach(this::print);
        indentCnt--;
    }

    private void print(ASMInstruction inst) {
        // todo delete this
        if (inst == null) return;
        printWithIndent(inst.toString());
    }
}

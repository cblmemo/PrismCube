package BackEnd;

import ASM.ASMBasicBlock;
import ASM.Operand.ASMConstString;
import ASM.ASMFunction;
import ASM.ASMModule;
import ASM.Instruction.ASMInstruction;
import ASM.Operand.GlobalSymbol.ASMGlobalBoolean;
import ASM.Operand.GlobalSymbol.ASMGlobalInteger;
import ASM.Operand.GlobalSymbol.ASMGlobalSymbol;
import Memory.Memory;

import java.io.PrintStream;

/**
 * This class print rv32i assembly to output file.
 * Some format were based on clang.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class ASMEmitter {
    private PrintStream ps;
    private int indentCnt = 0;
    private int functionCnt = 0;

    private final StringBuilder builder = new StringBuilder();

    private static PrintStream virtualStream = null;
    private static boolean print = false;
    private static boolean printVirtual = false;
    private static final int commentAlignLength = 50;
    private static final int alignLength = 15;

    public static int getAlignLength() {
        return alignLength;
    }

    public static void enable() {
        print = true;
    }

    public static void disable() {
        print = false;
    }

    public static void enableVirtual(PrintStream virtualStream) {
        ASMEmitter.virtualStream = virtualStream;
        printVirtual = true;
    }

    public static void disableVirtual() {
        printVirtual = false;
    }

    private void appendWithIndent(String str) {
        builder.append("\t".repeat(Math.max(0, indentCnt))).append(str).append("\n");
    }

    private String formatComment(String str, String comment) {
        int spaceNum = commentAlignLength - str.length() - 4 * indentCnt;
        assert spaceNum > 0;
        return str + " ".repeat(spaceNum) + "#" + comment;
    }

    private String formatPseudoOptions(String option, String value) {
        int spaceNum = alignLength - option.length() - 1;
        assert spaceNum > 0;
        return "." + option + " ".repeat(spaceNum) + value;
    }

    /**
     * This method print rv32i asm to PrintStream
     * specified by Memory.
     *
     * @see Memory
     */
    public void emit(Memory memory) {
        if (print) {
            ps = memory.getPrintStream();
            emit(memory.getAsmModule());
            ps.print(builder);
            builder.delete(0, builder.length());
        }
    }

    public void emitVirtual(Memory memory) {
        if (printVirtual) {
            assert virtualStream != null;
            ps = virtualStream;
            emit(memory.getAsmModule());
            ps.print(builder);
            builder.delete(0, builder.length());
        }
    }

    public String emitFunctionToString(ASMFunction function) {
        emit(function);
        String ret = builder.toString();
        builder.delete(0, builder.length());
        return ret;
    }

    private void emit(ASMModule module) {
        indentCnt++;
        appendWithIndent(".text");
        appendWithIndent(formatPseudoOptions("file", "\"src.mx\""));
        indentCnt--;
        module.getFunctions().values().forEach(this::emit);
        if (module.getGlobals().values().size() != 0) {
            indentCnt++;
            appendWithIndent("");
            appendWithIndent(formatPseudoOptions("section", ".sdata, \"aw\", @progbits"));
            indentCnt--;
            module.getGlobals().values().forEach(this::emit);
        }
        if (module.getStrings().values().size() != 0) {
            indentCnt++;
            appendWithIndent("");
            appendWithIndent(formatPseudoOptions("section", ".rodata.str1.1, \"aMS\", @progbits, 1"));
            indentCnt--;
            module.getStrings().values().forEach(this::emit);
        }
    }

    private void emit(ASMGlobalSymbol symbol) {
        String name = symbol.getSymbolName();
        appendWithIndent("");
        indentCnt++;
        appendWithIndent(formatComment(formatPseudoOptions("type", name + ", @object"), " @" + name));
        appendWithIndent(formatPseudoOptions("globl", symbol.getSymbolName()));
        appendWithIndent(formatPseudoOptions("p2align", "2"));
        indentCnt--;
        appendWithIndent(name + ":");
        indentCnt++;
        int value = symbol.getValue();
        if (symbol instanceof ASMGlobalBoolean) {
            appendWithIndent(formatComment(formatPseudoOptions("byte", Integer.toString(value)), " 0x" + value));
            appendWithIndent(formatPseudoOptions("size", symbol.getSymbolName() + ", 1"));
        } else if (symbol instanceof ASMGlobalInteger) {
            appendWithIndent(formatComment(formatPseudoOptions("word", Integer.toUnsignedString(value)), " 0x" + Integer.toUnsignedString(value, 16)));
            appendWithIndent(formatPseudoOptions("size", symbol.getSymbolName() + ", 4"));
        } else {
            appendWithIndent(formatPseudoOptions("word", Integer.toUnsignedString(value)));
            appendWithIndent(formatPseudoOptions("size", name + ", 4"));
        }
        indentCnt--;
    }

    private void emit(ASMConstString string) {
        String name = string.getName();
        appendWithIndent("");
        indentCnt++;
        appendWithIndent(formatComment(formatPseudoOptions("type", name + ", @object"), " @" + name));
        indentCnt--;
        appendWithIndent(name + ":");
        String value = string.getValue();
        int length = string.getLength();
        indentCnt++;
        appendWithIndent(formatPseudoOptions("asciz", "\"" + value + "\""));
        appendWithIndent(formatPseudoOptions("size", name + ", " + (length + 1)));
        indentCnt--;
    }

    private void emit(ASMFunction function) {
        appendWithIndent("");
        String funcName = function.getFunctionName();
        indentCnt++;
        appendWithIndent(formatComment(formatPseudoOptions("globl", funcName), " -- Begin Function " + funcName));
        appendWithIndent(formatPseudoOptions("p2align", "2"));
        appendWithIndent(formatPseudoOptions("type", funcName + ", @function"));
        indentCnt--;
        appendWithIndent(formatComment(funcName + ":", " @" + funcName));
        function.getBlocks().forEach(this::emit);
        String endLabel = ".Lfunc_end" + (functionCnt++);
        appendWithIndent(endLabel + ":");
        indentCnt++;
        appendWithIndent(formatPseudoOptions("size", funcName + ", " + endLabel + "-" + funcName));
        indentCnt--;
        appendWithIndent(formatComment("", " -- End Function"));
    }

    private void emit(ASMBasicBlock block) {
        appendWithIndent(block.getLabel() + ":");
        indentCnt++;
        block.getInstructions().forEach(this::emit);
        indentCnt--;
    }

    private void emit(ASMInstruction inst) {
        if (inst == null) { // used in printVirtual
            appendWithIndent("null: plus sp or minus sp");
            return;
        }
        appendWithIndent(inst.toString());
    }
}

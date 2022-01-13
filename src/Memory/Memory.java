package Memory;

import ASM.ASMModule;
import AST.ProgramNode;
import BackEnd.ASMPrinter;
import BackEnd.InstructionSelector;
import BackEnd.RegisterAllocator;
import Debug.ASTPrinter;
import Debug.MemoLog;
import Debug.ScopePrinter;
import FrontEnd.ConstStringCollector;
import FrontEnd.IRBuilder;
import FrontEnd.IRPrinter;
import IR.IRModule;
import IR.Operand.IRRegister;
import Utility.Scope.GlobalScope;
import Utility.error.ArgumentParseError;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;

import static Debug.MemoLog.log;

/**
 * This class stores every variable needed in compiler.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class Memory {
    private ParseTree parseTreeRoot;
    private final GlobalScope globalScope;
    private ProgramNode astRoot;
    private final IRModule irModule;
    private final ASMModule asmModule;

    // io
    private InputStream inputStream;
    private PrintStream printStream;
    private PrintStream debugStream;

    public Memory(String[] args) throws FileNotFoundException {
        globalScope = new GlobalScope(null);
        irModule = new IRModule();
        asmModule = new ASMModule();
        parseArgument(args);
    }

    private void err(String message) {
        throw new ArgumentParseError(message);
    }

    private enum Mode {
        NONE, SYNTAX, CODEGEN
    }

    private Mode mode = Mode.NONE;

    private void parseArgument(String[] args) throws FileNotFoundException {
        useDefaultSetup();
        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if (!Objects.equals(arg0.charAt(0), '-')) err("wrong argument format");
            switch (arg0) {
                case "-fsyntax-only" -> {
                    if (mode != Mode.NONE) err("argument conflict");
                    mode = Mode.SYNTAX;
                }
                case "-log-o" -> {
                    if (i == args.length - 1) err("missing argument");
                    String arg = args[++i];
                    log.SetOutPutFile(arg);
                }
                case "-log-level" -> {
                    String arg = args[++i];
                    switch (arg) {
                        case "trace" -> log.SetLogLevel(MemoLog.LogLevel.TraceLevel);
                        case "debug" -> log.SetLogLevel(MemoLog.LogLevel.DebugLevel);
                        case "info" -> log.SetLogLevel(MemoLog.LogLevel.InfoLevel);
                        case "error" -> log.SetLogLevel(MemoLog.LogLevel.ErrorLevel);
                        case "fatal" -> log.SetLogLevel(MemoLog.LogLevel.FatalLevel);
                        default -> err("wrong argument format");
                    }
                }
                case "-emit-llvm" -> IRPrinter.enable();
                case "-emit-asm" -> {
                    if (mode != Mode.NONE) err("argument conflict");
                    mode = Mode.CODEGEN;
                }
                case "-printV" -> {
                    if (i == args.length - 1) err("missing argument");
                    String arg1 = args[++i];
                    debugStream = new PrintStream(arg1);
                    ASMPrinter.enableVirtual();
                }
                case "-o" -> {
                    if (i == args.length - 1) err("missing argument");
                    String arg1 = args[++i];
                    printStream = new PrintStream(arg1);
                }
                case "-i" -> {
                    if (i == args.length - 1) err("missing argument");
                    String arg1 = args[++i];
                    inputStream = new FileInputStream(arg1);
                }
                case "-debug" -> {
                    ASTPrinter.enable();
                    ScopePrinter.enable();
                }
                case "-print-reg-name" -> IRRegister.printRegisterName();
                default -> err("wrong argument format");
            }
        }
        switch (mode) {
            case NONE -> err("missing mode");
            case SYNTAX -> {
                ConstStringCollector.disable();
                IRBuilder.disable();
                InstructionSelector.disable();
                RegisterAllocator.disable();
                ASMPrinter.disable();
            }
            case CODEGEN -> {
                ConstStringCollector.enable();
                IRBuilder.enable();
                InstructionSelector.enable();
                RegisterAllocator.enable();
                ASMPrinter.enable();
            }
        }
    }

    public void useDefaultSetup() {
        log.disableLog();
        printStream = System.out;
        inputStream = System.in;
        IRPrinter.disable();
        ASMPrinter.disableVirtual();
    }

    public void setParseTreeRoot(ParseTree parseTreeRoot) {
        this.parseTreeRoot = parseTreeRoot;
    }

    public ParseTree getParseTreeRoot() {
        return parseTreeRoot;
    }

    public void setASTRoot(ProgramNode astRoot) {
        this.astRoot = astRoot;
    }

    public ProgramNode getASTRoot() {
        return astRoot;
    }

    public GlobalScope getGlobalScope() {
        return globalScope;
    }

    public IRModule getIRModule() {
        return irModule;
    }

    public ASMModule getAsmModule() {
        return asmModule;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

    public PrintStream getDebugStream() {
        return debugStream;
    }
}

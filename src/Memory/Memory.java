package Memory;

import ASM.ASMModule;
import AST.ProgramNode;
import BackEnd.ASMEmitter;
import BackEnd.InstructionSelector;
import BackEnd.RegisterAllocator;
import Debug.ASTPrinter;
import Debug.MemoLog;
import Debug.ScopePrinter;
import FrontEnd.ConstStringCollector;
import FrontEnd.IRBuilder;
import FrontEnd.IREmitter;
import IR.IRModule;
import IR.Operand.IRRegister;
import MiddleEnd.Optimize;
import Utility.Scope.GlobalScope;
import Utility.error.ArgumentParseError;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Optional;

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

    public Memory() {
        globalScope = new GlobalScope(null);
        irModule = new IRModule();
        asmModule = new ASMModule();
    }

    private void err(String message) {
        throw new ArgumentParseError(message);
    }

    private enum Mode {
        NONE, SYNTAX, LLVM, CODEGEN
    }

    private static Mode mode = Mode.NONE;

    public static boolean codegen() {
        return mode == Mode.CODEGEN;
    }

    public enum Architecture {
        x86_64, x86_32
    }

    private static Architecture architecture = Architecture.x86_64;

    public static Architecture getArchitecture() {
        return architecture;
    }

    public Memory parse(String[] args) throws FileNotFoundException {
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
                    log.enableLog();
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
                    log.enableLog();
                }
                case "-emit-llvm" -> {
                    String arg = i + 1 < args.length && args[i + 1].charAt(0) == '-' ? "./bin/test.ll" : args[++i];
                    PrintStream irStream = new PrintStream(arg);
                    IREmitter.enable(irStream);
                }
                case "-llvm-only" -> {
                    if (mode != Mode.NONE) err("argument conflict");
                    mode = Mode.LLVM;
                }
                case "-emit-asm" -> {
                    if (mode != Mode.NONE) err("argument conflict");
                    mode = Mode.CODEGEN;
                }
                case "-O0" -> Optimize.setLevel(Optimize.OptimizeLevel.O0);
                case "-O1" -> Optimize.setLevel(Optimize.OptimizeLevel.O1);
                case "-O2" -> Optimize.setLevel(Optimize.OptimizeLevel.O2);
                case "-printV" -> {
                    String arg = (i + 1 < args.length && args[i + 1].charAt(0) != '-') ? args[++i] : "./bin/virtual.s";
                    PrintStream virtualStream = new PrintStream(arg);
                    ASMEmitter.enableVirtual(virtualStream);
                }
                case "-printO2IR" -> {
                    String arg = (i + 1 < args.length && args[i + 1].charAt(0) != '-') ? args[++i] : "./bin/opt.s";
                    PrintStream optStream = new PrintStream(arg);
                    IREmitter.enableOpt(optStream);
                }
                case "-arch" -> {
                    if (!(i + 1 < args.length && (Objects.equals(args[i + 1], "x86_64") || Objects.equals(args[i + 1], "x86_32")))) err("wrong arch argument");
                    architecture = Objects.equals(args[++i], "x86_64") ? Architecture.x86_64 : Architecture.x86_32;
                }
                case "-o" -> {
                    if (i == args.length - 1) err("missing argument");
                    String arg = args[++i];
                    printStream = new PrintStream(arg);
                }
                case "-i" -> {
                    if (i == args.length - 1) err("missing argument");
                    String arg = args[++i];
                    inputStream = new FileInputStream(arg);
                }
                case "-debug" -> {
                    ASTPrinter.enable();
                    ScopePrinter.enable();
                }
                case "-printInnerIR" -> IREmitter.enableDebug();
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
                ASMEmitter.disable();
                Optimize.setLevel(Optimize.OptimizeLevel.O0);
            }
            case LLVM -> {
                ConstStringCollector.enable();
                IRBuilder.enable();
                IREmitter.enable(printStream);
                InstructionSelector.disable();
                RegisterAllocator.disable();
                ASMEmitter.disable();
            }
            case CODEGEN -> {
                ConstStringCollector.enable();
                IRBuilder.enable();
                InstructionSelector.enable();
                RegisterAllocator.enable();
                ASMEmitter.enable();
            }
        }
        return this;
    }

    public void useDefaultSetup() {
        log.disableLog();
        printStream = System.out;
        inputStream = System.in;
        IREmitter.disable();
        ASMEmitter.disableVirtual();
        Optimize.setLevel(Optimize.OptimizeLevel.O0);
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
}

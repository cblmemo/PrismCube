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

    private void parseArgument(String[] args) throws FileNotFoundException {
        // todo use enum mode to avoid chaos enable and disable
        boolean syntaxOnly = false, setLogLevel = false, setLogFile = false, emitLLVM = false, codegen = false;
        useDefaultSetup();
        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if (!Objects.equals(arg0.charAt(0), '-')) err("wrong argument format");
            switch (arg0) {
                case "-fsyntax-only" -> {
                    if (emitLLVM || codegen) err("argument conflict");
                    syntaxOnly = true;
                    ConstStringCollector.disable();
                    IRBuilder.disable();
                    InstructionSelector.disable();
                    RegisterAllocator.disable();
                    ASMPrinter.disable();
                }
                case "-log" -> {
                    if (i == args.length - 1) err("missing argument");
                    String arg1 = args[++i];
                    if (!Objects.equals(arg1.charAt(0), '-')) err("wrong argument format");
                    switch (arg1) {
                        case "-o" -> {
                            if (setLogFile) err("already set log file");
                            if (i == args.length - 1) err("missing argument");
                            String arg2 = args[++i];
                            log.SetOutPutFile(arg2);
                            setLogFile = true;
                        }
                        case "-level" -> {
                            if (setLogLevel) err("already set log level");
                            if (i == args.length - 1) err("missing argument");
                            String arg2 = args[++i];
                            switch (arg2) {
                                case "trace" -> log.SetLogLevel(MemoLog.LogLevel.TraceLevel);
                                case "debug" -> log.SetLogLevel(MemoLog.LogLevel.DebugLevel);
                                case "info" -> log.SetLogLevel(MemoLog.LogLevel.InfoLevel);
                                case "error" -> log.SetLogLevel(MemoLog.LogLevel.ErrorLevel);
                                case "fatal" -> log.SetLogLevel(MemoLog.LogLevel.FatalLevel);
                                default -> err("wrong argument format");
                            }
                            setLogLevel = true;
                        }
                        default -> err("wrong argument format");
                    }
                }
                case "-emit-llvm" -> {
                    if (syntaxOnly || codegen) err("argument conflict");
                    IRPrinter.enable();
                    InstructionSelector.disable();
                    RegisterAllocator.disable();
                    ASMPrinter.disable();
                    emitLLVM = true;
                }
                case "-emit-asm" -> {
                    if (syntaxOnly || emitLLVM) err("argument conflict");
                    codegen = true;
                    // -emit-asm is default setup
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
    }

    public void useDefaultSetup() {
        log.disableLog();
        printStream = System.out;
        inputStream = System.in;
        ConstStringCollector.enable();
        IRBuilder.enable();
        IRPrinter.disable();
        InstructionSelector.enable();
        RegisterAllocator.enable();
        ASMPrinter.enable();
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

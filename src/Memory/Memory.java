package Memory;

import AST.ProgramNode;
import Debug.MemoLog;
import IR.IRModule;
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
    private ProgramNode astRoot;
    private final IRModule irModule;
    private final GlobalScope globalScope;
    private InputStream inputStream;
    private PrintStream printStream;

    // flags
    private boolean AST = true;
    private boolean Scope = true;
    private boolean IR = true;
    private boolean constexpr = true;
    private boolean IRBuild = true;

    public Memory(String[] args) throws FileNotFoundException {
        globalScope = new GlobalScope(null);
        irModule = new IRModule();
        parseArgument(args);
    }

    private void err(String message) {
        throw new ArgumentParseError(message);
    }

    private void parseArgument(String[] args) throws FileNotFoundException {
        boolean syntaxOnly = false, setLogLevel = false, setLogFile = false, debug = false, receiveFromFile = false, printToFile = false, emitLLVM = false;
        if (args.length == 0) useDefaultSetup();
        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if (!Objects.equals(arg0.charAt(0), '-')) err("wrong argument format");
            switch (arg0) {
                case "-fsyntax-only" -> {
                    if (emitLLVM) err("argument conflict");
                    disableIRBuild();
                    disableIRPrinter();
                    syntaxOnly = true;
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
                    if (syntaxOnly) err("argument conflict");
                    emitLLVM = true;
                }
                case "-o" -> {
                    if (i == args.length - 1) err("missing argument");
                    String arg1 = args[++i];
                    printStream = new PrintStream(arg1);
                    printToFile = true;
                }
                case "-i" -> {
                    if (i == args.length - 1) err("missing argument");
                    String arg1 = args[++i];
                    inputStream = new FileInputStream(arg1);
                    receiveFromFile = true;
                }
                case "-debug" -> {
                    debug = true;
                }
                default -> err("wrong argument format");
            }
        }
        if (!setLogFile) log.disableLog();
        if (!setLogLevel) log.SetLogLevel(MemoLog.LogLevel.DebugLevel);
        if (!debug) {
            disableASTPrinter();
            disableScopePrinter();
        }
        if (!emitLLVM) disableIRPrinter();
        if (!printToFile) printStream = System.out;
        if (!receiveFromFile) inputStream = System.in;
    }

    public void useDefaultSetup() {
        log.SetLogLevel(MemoLog.LogLevel.DebugLevel);
        log.SetOutPutFile("bin/log.txt");

        // receive source code from stdin by default
        inputStream = System.in;

        // disable printer for debug and constexpr optimize
        disableASTPrinter();
        disableScopePrinter();
        disableConstExprCalculate();
    }

    public void disableASTPrinter() {
        AST = false;
    }

    public boolean printAST() {
        return AST;
    }

    public void disableScopePrinter() {
        Scope = false;
    }

    public boolean printScope() {
        return Scope;
    }

    public void disableIRPrinter() {
        IR = false;
    }

    public boolean printIR() {
        return IR;
    }

    public void disableConstExprCalculate() {
        constexpr = false;
    }

    public boolean calculateConstexpr() {
        return constexpr;
    }

    public void disableIRBuild() {
        IRBuild = false;
    }

    public boolean buildIR() {
        return IRBuild;
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

    public InputStream getInputStream() {
        return inputStream;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }
}

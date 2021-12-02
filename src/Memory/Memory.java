package Memory;

import AST.ProgramNode;
import Debug.MemoLog;
import IR.IRModule;
import Utility.Scope.GlobalScope;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.InputStream;

import static Debug.MemoLog.log;

/**
 * This class stores every variable needed in compiler.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class Memory {
    private final String[] commandlineArguments;
    private ParseTree parseTreeRoot;
    private ProgramNode astRoot;
    private final IRModule irModule;
    private final GlobalScope globalScope;
    private InputStream inputStream;

    // flags
    private boolean AST = true;
    private boolean Scope = true;
    private boolean IR = true;
    private boolean constexpr = true;
    private boolean IRBuild = true;

    public Memory(String[] commandlineArguments) {
        this.commandlineArguments = commandlineArguments;
        globalScope = new GlobalScope(null);
        irModule = new IRModule();
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

    public void semanticOnly() {
        disableIRBuild();
        disableIRPrinter();
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

    public String[] getCommandlineArguments() {
        return commandlineArguments;
    }
}

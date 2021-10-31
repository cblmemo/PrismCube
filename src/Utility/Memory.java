package Utility;

import AST.ProgramNode;
import Utility.Scope.GlobalScope;
import org.antlr.v4.runtime.tree.ParseTree;

public class Memory {
    private String inputFileName;
    private ParseTree parseTreeRoot;
    private ProgramNode ASTRoot;
    private GlobalScope globalScope = new GlobalScope(null);
    private boolean receiveFromFile;

    public Memory(String inputFileName) {
        this.inputFileName = inputFileName;
        receiveFromFile = true;
    }

    public Memory() {
        receiveFromFile = false;
    }

    public boolean receiveFromFile() {
        return receiveFromFile;
    }

    public void setParseTreeRoot(ParseTree parseTreeRoot) {
        this.parseTreeRoot = parseTreeRoot;
    }

    public void setASTRoot(ProgramNode ASTRoot) {
        this.ASTRoot = ASTRoot;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public ParseTree getParseTreeRoot() {
        return parseTreeRoot;
    }

    public ProgramNode getASTRoot() {
        return ASTRoot;
    }

    public GlobalScope getGlobalScope() {
        return globalScope;
    }
}

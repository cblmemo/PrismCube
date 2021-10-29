package FrontEnd;

import AST.ProgramNode;
import Utility.Scope.GlobalScope;
import org.antlr.v4.runtime.tree.ParseTree;

public class Memory {
    private String inputFileName;
    private ParseTree parseTreeRoot;
    private ProgramNode ASTRoot;
    private GlobalScope globalScope = new GlobalScope(null);

    public Memory(String inputFileName) {
        this.inputFileName = inputFileName;
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

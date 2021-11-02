package Memory;

import AST.ProgramNode;
import Utility.Scope.GlobalScope;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * This class stores every variable needed in compiler.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class Memory {
    private ParseTree parseTreeRoot;
    private ProgramNode ASTRoot;
    private final GlobalScope globalScope = new GlobalScope(null);
    private final InputStream inputStream;

    // receive source code from file
    public Memory(String inputFileName) throws FileNotFoundException {
        inputStream = new FileInputStream(inputFileName);
    }

    // receive source code from stdin
    public Memory() {
        inputStream = System.in;
    }

    public void setParseTreeRoot(ParseTree parseTreeRoot) {
        this.parseTreeRoot = parseTreeRoot;
    }

    public ParseTree getParseTreeRoot() {
        return parseTreeRoot;
    }

    public void setASTRoot(ProgramNode ASTRoot) {
        this.ASTRoot = ASTRoot;
    }

    public ProgramNode getASTRoot() {
        return ASTRoot;
    }

    public GlobalScope getGlobalScope() {
        return globalScope;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}

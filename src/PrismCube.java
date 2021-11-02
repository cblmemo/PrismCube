import Debug.ASTPrinter;
import Debug.MemoLog;
import Debug.ScopePrinter;
import FrontEnd.ASTBuilder;
import FrontEnd.Preprocessor;
import FrontEnd.SemanticChecker;
import FrontEnd.SymbolCollector;
import Memory.Memory;
import Utility.error.error;

import static Debug.MemoLog.log;

/**
 * This class compiles source code.
 * Now support semantic check.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class PrismCube {
    public static void main(String[] args) throws Exception {
        try {
            log.SetLogLevel(MemoLog.LogLevel.InfoLevel);
            log.SetOutPutFile("log.txt");

            Memory memory = new Memory();

            new Preprocessor().preprocess(memory);

            new ASTBuilder().build(memory);

//            new ASTPrinter().print(memory);

            new SymbolCollector().collect(memory);

//            new ScopePrinter().print(memory);

            new SemanticChecker().check(memory);

        } catch (error err) {
            System.err.println(err.toString());
            throw new RuntimeException();
        }
    }
}

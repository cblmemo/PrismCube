import Debug.ASTPrinter;
import Debug.MemoLog;
import Debug.ScopePrinter;
import FrontEnd.Preprocessor;
import FrontEnd.ASTBuilder;
import FrontEnd.SymbolCollector;
import FrontEnd.SemanticChecker;
import Utility.Memory;
import Utility.error.error;

import static Debug.MemoLog.log;

public class PrismCube {
    public static void main(String[] args) throws Exception {
        try {
            log.SetLogLevel(MemoLog.LogLevel.InfoLevel);
            log.SetOutPutFile("log.txt");

            Memory memory = new Memory("test.mx");

            new Preprocessor().preprocess(memory);

            new ASTBuilder().build(memory);

            new ASTPrinter().print(memory);

            new SymbolCollector().collect(memory);

            new ScopePrinter().print(memory);

            new SemanticChecker().check(memory);

        } catch (error err) {
            System.err.println(err.toString());
            throw new RuntimeException();
        }
    }
}

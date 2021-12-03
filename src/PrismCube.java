import Debug.ASTPrinter;
import Debug.ScopePrinter;
import FrontEnd.*;
import Memory.Memory;
import Utility.error.error;

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
            Memory memory = new Memory(args);
            memory.useDefaultSetup();
//            memory.semanticOnly();

            new ArgumentParser().parse(memory);
            new Preprocessor().preprocess(memory);
            new ASTBuilder().build(memory);
            new ASTPrinter().print(memory);

            new SymbolCollector().collect(memory);
            new ScopePrinter().print(memory);
            new SemanticChecker().check(memory);

            new ConstStringCollector().collect(memory);
            new ConstExprCalculator().calculate(memory);
            new IRBuilder().build(memory);
            new IRPrinter().print(memory);

        } catch (error err) {
            System.err.println(err.toString());
            throw new RuntimeException();
        }
    }
}

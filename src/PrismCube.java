import FrontEnd.*;
import BackEnd.*;
import Debug.*;
import Memory.Memory;
import Utility.error.error;

/**
 * This class compiles source code to rv32i asm.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class PrismCube {
    public static void main(String[] args) throws Exception {
        try {
            Memory memory = new Memory(args);

            new Preprocessor().preprocess(memory);
            new ASTBuilder().build(memory);
            new ASTPrinter().print(memory);

            new SymbolCollector().collect(memory);
            new ScopePrinter().print(memory);
            new SemanticChecker().check(memory);

            new ConstStringCollector().collect(memory);
            new IRBuilder().build(memory);
            new IRPrinter().print(memory);

            new InstructionSelector().select(memory);
            new ASMPrinter().printVirtual(memory);
            new RegisterAllocator().allocate(memory);
            new ASMPrinter().print(memory);
        } catch (error err) {
            System.err.println(err.toString());
            throw new RuntimeException();
        }
    }
}

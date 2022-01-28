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
            Memory memory = new Memory().parse(args);

            new Preprocessor().preprocess(memory);
            new ASTBuilder().build(memory);
            new ASTPrinter().print(memory);

            new SymbolCollector().collect(memory);
            new ScopePrinter().print(memory);
            new SemanticChecker().check(memory);

            new ConstStringCollector().collect(memory);
            new IRBuilder().build(memory);
            new IREmitter().emit(memory);

            new InstructionSelector().select(memory);
            new ASMEmitter().emitVirtual(memory);
            new RegisterAllocator().allocate(memory);
            new ASMEmitter().emit(memory);
        } catch (error err) {
            System.err.println(err.toString());
            throw new RuntimeException();
        }
    }
}

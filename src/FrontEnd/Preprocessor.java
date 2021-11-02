package FrontEnd;

import Memory.Memory;
import Parser.MxStarLexer;
import Parser.MxStarParser;
import Utility.MxStarErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;

import static Debug.MemoLog.log;

/**
 * This class accepts input stream, and calls antlr4
 * lexer and parser to generate a parse tree.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class Preprocessor {

    /**
     * This method builds parse tree and store it
     * to memory.
     *
     * @see Memory
     */
    public void preprocess(Memory memory) throws IOException {
        log.Infof("Preprocess started.\n");

        InputStream input = memory.getInputStream();

        // lexer
        MxStarLexer lexer = new MxStarLexer(CharStreams.fromStream(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new MxStarErrorListener());

        // parser
        MxStarParser parser = new MxStarParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new MxStarErrorListener());

        memory.setParseTreeRoot(parser.program());

        log.Infof("Preprocess finished.\n");
    }
}

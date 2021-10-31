package FrontEnd;

import Parser.MxStarLexer;
import Parser.MxStarParser;
import Utility.Memory;
import Utility.MxStarErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;

import static Debug.MemoLog.log;

public class Preprocessor {
    public void preprocess(Memory memory) throws IOException {
        log.Infof("Preprocess started.\n");

        InputStream input;
        if (memory.receiveFromFile()) input = new FileInputStream(memory.getInputFileName());
        else input = System.in;

        MxStarLexer lexer = new MxStarLexer(CharStreams.fromStream(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new MxStarErrorListener());

        MxStarParser parser = new MxStarParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new MxStarErrorListener());
        memory.setParseTreeRoot(parser.program());

        log.Infof("Preprocess finished.\n");
    }
}

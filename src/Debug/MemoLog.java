package Debug;

import Utility.error.LogError;

import java.io.*;

public class MemoLog {
    public static MemoLog log = new MemoLog();

    public enum LogLevel {
        TraceLevel,
        InfoLevel,
        ErrorLevel
    }

    private PrintStream ps = System.out;
    private LogLevel level = LogLevel.InfoLevel;

    public void SetPrintStream(PrintStream ps) {
        this.ps = ps;
    }

    public void SetOutPutFile(String fileName) {
        try {
            ps = new PrintStream(new FileOutputStream(fileName));
        } catch (FileNotFoundException err) {
            throw new LogError("[SetOutPutFile] failed. File not found.\n");
        }
    }

    public void SetLogLevel(LogLevel level) {
        this.level = level;
    }

    public void Tracef(String format, Object... args) {
        if (level.ordinal() <= LogLevel.TraceLevel.ordinal()) {
            ps.printf(format, args);
        }
    }

    public void Infof(String format, Object... args) {
        if (level.ordinal() <= LogLevel.InfoLevel.ordinal()) {
            ps.printf(format, args);
        }
    }

    public void Errorf(String format, Object... args) {
        if (level.ordinal() <= LogLevel.ErrorLevel.ordinal()) {
            String message = String.format(format, args);
            ps.print(message);
            throw new LogError(message);
        }
    }
}

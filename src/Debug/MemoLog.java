package Debug;

import Utility.error.LogError;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * This class implements a simple log
 * with level.
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class MemoLog {
    public static MemoLog log = new MemoLog();

    public enum LogLevel {
        TraceLevel,
        DebugLevel,
        InfoLevel,
        ErrorLevel,
        FatalLevel
    }

    private PrintStream ps = System.out;
    private LogLevel level = LogLevel.InfoLevel;
    private boolean disabled = false;

    private static final String redPrefix = "\033[30m";
    private static final String suffix = "\033[0m";

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

    public void enableLog() {
        disabled = false;
    }

    public void disableLog() {
        disabled = true;
    }

    public void Tracef(String format, Object... args) {
        if (disabled) return;
        if (level.ordinal() <= LogLevel.TraceLevel.ordinal()) {
            ps.printf("[trace] " + format, args);
        }
    }

    public void Debugf(String format, Object... args) {
        if (disabled) return;
        if (level.ordinal() <= LogLevel.DebugLevel.ordinal()) {
            ps.printf("[debug] " + format, args);
        }
    }

    public void Infof(String format, Object... args) {
        if (disabled) return;
        if (level.ordinal() <= LogLevel.InfoLevel.ordinal()) {
            ps.printf("[info] " + format, args);
        }
    }

    public void Errorf(String format, Object... args) {
        if (disabled) return;
        if (level.ordinal() <= LogLevel.ErrorLevel.ordinal()) {
            ps.printf(redPrefix + "[error] " + format + suffix, args);
        }
    }

    public void Fatalf(String format, Object... args) {
        if (disabled) return;
        if (level.ordinal() <= LogLevel.ErrorLevel.ordinal()) {
            String message = String.format(format, args);
            ps.print(redPrefix + "[fatal] " + message + suffix);
            throw new LogError(message);
        }
    }
}

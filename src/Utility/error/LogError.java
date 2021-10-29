package Utility.error;

import Utility.Cursor;

public class LogError extends error {
    public LogError(String message) {
        super(message, new Cursor(-2, -2));
    }
}

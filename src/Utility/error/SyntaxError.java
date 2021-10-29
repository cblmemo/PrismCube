package Utility.error;

import Utility.Cursor;

public class SyntaxError extends error {
    public SyntaxError(String message, Cursor cursor) {
        super("Syntax Error: " + message, cursor);
    }
}

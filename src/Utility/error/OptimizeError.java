package Utility.error;

import Utility.Cursor;

public class OptimizeError extends error {
    public OptimizeError(String message) {
        super("OptimizeError: " + message, new Cursor(-1, -1));
    }
}

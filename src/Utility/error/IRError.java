package Utility.error;

import Utility.Cursor;

public class IRError extends error {
    public IRError(String message) {
        super("IRError: " + message, new Cursor(-1, -1));
    }
}

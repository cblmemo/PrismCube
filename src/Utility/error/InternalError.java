package Utility.error;

import Utility.Cursor;

public class InternalError extends error {
    public InternalError(String message, Cursor cursor) {
        super("Internal Error: " + message, cursor);
    }
}

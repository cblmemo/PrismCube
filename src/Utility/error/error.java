package Utility.error;

import Utility.Cursor;

abstract public class error extends RuntimeException {
    private final Cursor cursor;
    private final String message;

    public error(String message, Cursor cursor) {
        this.message = message;
        this.cursor = cursor;
    }

    @Override
    public String toString() {
        return "error [" + message + "] at " + cursor.toString();
    }
}

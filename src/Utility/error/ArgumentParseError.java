package Utility.error;

import Utility.Cursor;

public class ArgumentParseError extends error {
    public ArgumentParseError(String message) {
        super("ArgumentError: " + message, new Cursor(-1, -1));
    }
}

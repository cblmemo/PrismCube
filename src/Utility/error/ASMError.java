package Utility.error;

import Utility.Cursor;

public class ASMError extends error {
    public ASMError(String message) {
        super("ASMError: " + message, new Cursor(-1, -1));
    }
}

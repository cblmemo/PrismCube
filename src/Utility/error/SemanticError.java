package Utility.error;

import Utility.Cursor;

public class SemanticError extends error {
    public SemanticError(String message, Cursor cursor) {
        super("Semantic Error: " + message, cursor);
    }
}

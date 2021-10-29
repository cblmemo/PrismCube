package Utility;

import org.antlr.v4.runtime.ParserRuleContext;

public class Cursor {
    private final int row, col;

    public Cursor(int row, int column) {
        this.row = row;
        this.col = column;
    }

    public Cursor(ParserRuleContext ctx) {
        this(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    public String toString() {
        if (row == -1 && col == -1) return "origin";
        if (row == -2 && col == -2) return "log";
        return "(" + row + ", " + col + ")";
    }
}

package Utility;

import org.antlr.v4.runtime.ParserRuleContext;

public class Cursor {
    private final int row;
    private final int column;

    public Cursor(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public Cursor(ParserRuleContext ctx) {
        this(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    public String toString() {
        if (row == -1 && column == -1) return "origin";
        if (row == -2 && column == -2) return "log";
        return "(" + row + ", " + column + ")";
    }
}

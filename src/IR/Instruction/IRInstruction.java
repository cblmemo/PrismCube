package IR.Instruction;

import FrontEnd.IRVisitor;

abstract public class IRInstruction {
    private static final boolean useAlign = true;

    private String comment = null;

    public static boolean useAlign() {
        return useAlign;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment == null ? "" : "; " + comment;
    }

    abstract public String toString();

    abstract public void accept(IRVisitor visitor);
}

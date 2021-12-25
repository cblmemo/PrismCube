package IR.Instruction;

import FrontEnd.IRVisitor;

abstract public class IRInstruction {
    private String comment = null;

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment == null ? "" : "; " + comment;
    }

    abstract public String toString();

    abstract public void accept(IRVisitor visitor);
}

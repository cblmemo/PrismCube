package Utility.Scope;

import IR.Operand.IRRegister;

abstract public class MethodScope extends Scope {
    // for ir
    private IRRegister thisPtrRegister;

    public MethodScope(Scope parentScope) {
        super(parentScope);
    }

    public void setThisPtrRegister(IRRegister thisPtrRegister) {
        this.thisPtrRegister = thisPtrRegister;
    }

    public IRRegister getThisPtrRegister() {
        return thisPtrRegister;
    }
}

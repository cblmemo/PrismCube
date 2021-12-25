package ASM;

import ASM.Instruction.*;

public interface ASMVisitor {
    void visit(ASMModule module);
    void visit(ASMFunction function);
    void visit(ASMBasicBlock block);

    void visit(ASMPseudoInstruction inst);
    void visit(ASMArithmeticInstruction inst);
    void visit(ASMLoadInstruction inst);
    void visit(ASMStoreInstruction inst);
}

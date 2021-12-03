package IR;

import IR.Instruction.*;
import IR.Operand.IRConstString;

public interface IRVisitor {
    void visit(IRModule module);
    void visit(IRGlobalDefine define);
    void visit(IRFunction function);
    void visit(IRBasicBlock block);
    void visit(IRConstString string);

    void visit(IRBrInstruction inst);
    void visit(IRCallInstruction inst);
    void visit(IRLoadInstruction inst);
    void visit(IRReturnInstruction inst);
    void visit(IRAllocaInstruction inst);
    void visit(IRStoreInstruction inst);
    void visit(IRBinaryInstruction inst);
    void visit(IRIcmpInstruction inst);
    void visit(IRTruncInstruction inst);
    void visit(IRZextInstruction inst);
    void visit(IRGetelementptrInstruction inst);
    void visit(IRBitcastInstruction inst);
}

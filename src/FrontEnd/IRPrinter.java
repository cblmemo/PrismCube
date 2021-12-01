package FrontEnd;

import IR.*;
import IR.Instruction.*;
import IR.Operand.IRConstString;
import Memory.Memory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * This class print LLVM-IR to output file,
 * and could run by the following command:
 * ----------------------------------------
 * $ llvm-link test.ll builtin.ll -o a.out
 * $ lli a.out
 * ----------------------------------------
 * clang version: 6.0.0-1ubuntu2 (tags/RELEASE_600/final)
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class IRPrinter implements IRVisitor {
    private PrintStream ps;

    public void print(Memory memory) throws FileNotFoundException {
        if (memory.printIR()) {
            ps = new PrintStream(new FileOutputStream("bin/test.ll"));
            memory.getIRModule().accept(this);
        }
    }

    @Override
    public void visit(IRModule module) {
        module.getBuiltinFunctions().forEach((name, func) -> {
            if (func.hasCalled()) func.accept(this);
        });
        ps.println();
        module.getStrings().forEach((name, string) -> string.accept(this));
        ps.println();
        module.getGlobalDefines().forEach((name, define) -> define.accept(this));
        ps.println(module.getLLVMGlobalConstructors());
        ps.println();
        module.getFunctions().forEach((name, func) -> func.accept(this));
        ps.println();
        module.getGlobalConstructor().accept(this);
        module.getSingleInitializeFunctions().forEach(func -> func.accept(this));
    }

    @Override
    public void visit(IRGlobalDefine define) {
        ps.println(define.toString());
    }

    @Override
    public void visit(IRFunction function) {
        if (function.isDeclare()) ps.println(function.getDeclare());
        else {
            ps.println(function.getDefineAndPrefix());
            function.getBlocks().forEach(block -> block.accept(this));
            ps.println(function.getSuffix());
            ps.println();
        }
    }

    @Override
    public void visit(IRBasicBlock block) {
        if (!block.getInstructions().isEmpty()) {
            ps.print(block.getLabel().toBasicBlockLabel());
            ps.println(" ".repeat(50 - block.getLabel().toBasicBlockLabel().length()) + block.getPreds());
            block.getInstructions().forEach(instruction -> {
                // indent non-label instructions
                ps.print("\t");
                instruction.accept(this);
            });
            if (!block.isReturnBlock()) ps.println();
        }
    }

    @Override
    public void visit(IRConstString string) {
        ps.println(string.toInitValueStr());
    }

    @Override
    public void visit(IRBrInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }

    @Override
    public void visit(IRCallInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }

    @Override
    public void visit(IRLoadInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }

    @Override
    public void visit(IRReturnInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }

    @Override
    public void visit(IRAllocaInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }

    @Override
    public void visit(IRStoreInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }

    @Override
    public void visit(IRBinaryInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }

    @Override
    public void visit(IRIcmpInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }

    @Override
    public void visit(IRTruncInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }

    @Override
    public void visit(IRZextInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }
}

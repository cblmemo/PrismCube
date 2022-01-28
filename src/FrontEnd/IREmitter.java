package FrontEnd;

import IR.*;
import IR.Instruction.*;
import IR.Operand.IRConstString;
import IR.TypeSystem.IRStructureType;
import Memory.Memory;

import java.io.PrintStream;

/**
 * This class print llvm ir to output file,
 * and could run by the following command:
 * ----------------------------------------
 * $ clang test.ll builtin.ll -o a.out
 * $ ./a.out
 * ----------------------------------------
 * or:
 * (not recommend since lli has some bug
 * when encounter malloc)
 * ----------------------------------------
 * $ llvm-link test.ll builtin.ll -o link.ll
 * $ lli link.ll
 * ----------------------------------------
 * clang version: 6.0.0-1ubuntu2 (tags/RELEASE_600/final)
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class IREmitter implements IRVisitor {
    private PrintStream ps;

    private static boolean print = false;
    private static PrintStream irStream = null;

    public static void enable(PrintStream irStream) {
        IREmitter.irStream = irStream;
        print = true;
    }

    public static void disable() {
        print = false;
    }

    /**
     * This method print llvm ir to PrintStream
     * specified by Memory.
     *
     * @see Memory
     */
    public void emit(Memory memory) {
        if (print) {
            ps = irStream;
            memory.getIRModule().accept(this);
        }
    }

    @Override
    public void visit(IRModule module) {
        ps.println(IRModule.getLLVMDetails());
        module.getBuiltinFunctions().forEach((name, func) -> {
            if (func.hasCalled()) func.accept(this);
        });
        module.getStrings().values().forEach(string -> string.accept(this));
        if (module.getStrings().size() != 0) ps.println();
        module.getClasses().forEach(type -> type.accept(this));
        if (module.getClasses().size() != 0) ps.println();
        module.getGlobalDefines().values().forEach(define -> define.accept(this));
        ps.println(module.getLLVMGlobalConstructors());
        ps.println();
        module.getFunctions().values().forEach(func -> func.accept(this));
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
        if (!block.isEmpty()) {
            ps.print(block.getLabel().toBasicBlockLabel());
            ps.println(" ".repeat(60 - block.getLabel().toBasicBlockLabel().length()) + block.getPreds());
            if (block.isEntryBlock()) block.getAllocas().forEach(alloca -> {
                ps.print("\t");
                alloca.accept(this);
            });
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
    public void visit(IRStructureType type) {
        ps.println(type.getClassDeclare());
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

    @Override
    public void visit(IRGetelementptrInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }

    @Override
    public void visit(IRBitcastInstruction inst) {
        ps.print(inst.toString());
        ps.println(inst.getComment());
    }
}

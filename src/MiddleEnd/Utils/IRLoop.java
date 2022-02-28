package MiddleEnd.Utils;

import IR.IRBasicBlock;
import IR.Instruction.IRInstruction;
import IR.Operand.*;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

public class IRLoop {
    private final IRBasicBlock header;
    private IRLoop parentLoop = null;
    private final LinkedHashSet<IRBasicBlock> terminals = new LinkedHashSet<>();
    private final LinkedHashSet<IRBasicBlock> nodes = new LinkedHashSet<>();
    private final LinkedHashSet<IRLoop> innerLoops = new LinkedHashSet<>();
    private int depth = -1;

    public IRLoop(IRBasicBlock header) {
        this.header = header;
        nodes.add(header);
    }

    public void addTerminal(IRBasicBlock terminal) {
        terminals.add(terminal);
        nodes.add(terminal);
    }

    public void addNode(IRBasicBlock node) {
        nodes.add(node);
    }

    public void setParentLoop(IRLoop parentLoop) {
        this.parentLoop = parentLoop;
    }

    public IRLoop getParentLoop() {
        return parentLoop;
    }

    public boolean addInnerLoop(IRLoop loop) {
        if (innerLoops.contains(loop)) return false;
        innerLoops.add(loop);
        loop.setParentLoop(this);
        return true;
    }

    public IRBasicBlock getHeader() {
        return header;
    }

    public boolean containsNode(IRBasicBlock node) {
        return nodes.contains(node);
    }

    public boolean invariantInThisLoop(IROperand operand) {
        return operand instanceof IRConstNumber;
    }

    public int extractInvariantOperand(IROperand operand) {
        assert operand instanceof IRConstNumber;
        return ((IRConstNumber) operand).getIntValue();
    }

    public void forEachNonHeaderNode(Consumer<IRBasicBlock> consumer) {
        nodes.forEach(node -> {
            if (node != header) consumer.accept(node);
        });
    }

    public void forEachLoopInstruction(Consumer<IRInstruction> consumer) {
        nodes.forEach(node -> node.forEachInstruction(consumer));
    }

    public void forEachInnerLoop(Consumer<IRLoop> consumer) {
        innerLoops.forEach(consumer);
    }

    public void setDepth(int depth) {
        this.depth = Math.max(this.depth, depth);
    }

    public void simplifyLoopNestTree() {
        innerLoops.removeIf(loop -> loop.depth > depth + 1);
        innerLoops.forEach(IRLoop::simplifyLoopNestTree);
    }

    @Override
    public String toString() {
        return "IRLoop: { header: " + header + ", nodes: " + nodes + ", terminals: " + terminals + " }";
    }
}

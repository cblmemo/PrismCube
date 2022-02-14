package ASM;

import ASM.Operand.ASMLabel;
import ASM.Operand.ASMPhysicalRegister;
import ASM.Operand.ASMVirtualRegister;
import IR.IRBasicBlock;
import IR.IRFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static Debug.MemoLog.log;

public class ASMFunction {
    private final String functionName;
    private final ArrayList<ASMBasicBlock> blocks = new ArrayList<>();
    private final LinkedHashMap<IRBasicBlock, ASMBasicBlock> blockMap = new LinkedHashMap<>();
    private final ASMBasicBlock entryBlock;
    private final LinkedHashMap<ASMPhysicalRegister, ASMVirtualRegister> calleeSaves = new LinkedHashMap<>();
    private final ASMStackFrame stackFrame = new ASMStackFrame();
    private final ASMLabel label;

    public ASMFunction(IRFunction function) {
        this.functionName = function.getFunctionName();
        function.getBlocks().forEach(irBlock -> {
            ASMBasicBlock asmBlock = new ASMBasicBlock(this, irBlock.getLabelName());
            blockMap.put(irBlock, asmBlock);
            blocks.add(asmBlock);
        });
        // inherit predecessors and successors from ir basic block
        function.getBlocks().forEach(irBlock -> {
            ASMBasicBlock asmBlock = blockMap.get(irBlock);
            irBlock.getPredecessors().forEach(pred -> asmBlock.addPredecessor(blockMap.get(pred)));
            irBlock.getSuccessors().forEach(succ -> asmBlock.addSuccessor(blockMap.get(succ)));
        });
        log.Debugf("start request alloca for function %s\n", functionName);
        function.getEntryBlock().getAllocas().forEach(stackFrame::requestAlloca);
        this.entryBlock = blockMap.get(function.getEntryBlock());
        this.label = new ASMLabel(function.getFunctionName(), null);
    }

    // constructor for builtin functions
    public ASMFunction(String functionName) {
        this.functionName = functionName;
        this.entryBlock = null;
        this.label = new ASMLabel(functionName, null);
    }

    public void addCalleeSave(ASMPhysicalRegister reg, ASMVirtualRegister calleeSave) {
        calleeSaves.put(reg, calleeSave);
    }

    public LinkedHashMap<ASMPhysicalRegister, ASMVirtualRegister> getCalleeSaves() {
        return calleeSaves;
    }

    public ASMLabel getBasicBlockLabel(IRBasicBlock block) {
        assert blockMap.containsKey(block);
        return blockMap.get(block).getLabel();
    }

    public ASMLabel getLabel() {
        return label;
    }

    public ASMBasicBlock getEntryBlock() {
        return entryBlock;
    }

    public String getFunctionName() {
        return functionName;
    }

    public ASMBasicBlock getASMBasicBlock(IRBasicBlock block) {
        assert blockMap.containsKey(block);
        return blockMap.get(block);
    }

    public ArrayList<ASMBasicBlock> getBlocks() {
        return blocks;
    }

    public ASMStackFrame getStackFrame() {
        return stackFrame;
    }

    private final LinkedHashMap<ASMBasicBlock, Integer> block2serial = new LinkedHashMap<>();
    private int N = -1;
    private final ArrayList<Boolean> mark = new ArrayList<>();
    private final ArrayList<ASMBasicBlock> sorted = new ArrayList<>();

    private void topo(int i) {
        log.Tracef("topo %d\n", i);
        if (!mark.get(i)) {
            mark.set(i, true);
            blocks.get(i).getSuccessors().forEach(succ -> topo(block2serial.get(succ)));
            for (int j = 0; j < blocks.size(); j++) assert sorted.get(j) != blocks.get(i);
            sorted.set(--N, blocks.get(i));
        }
    }

    public ArrayList<ASMBasicBlock> getTopologicalOrder() {
        if (!sorted.isEmpty()) return sorted;
        N = blocks.size();
        for (int i = 0; i < N; i++) {
            mark.add(false);
            sorted.add(null);
            block2serial.put(blocks.get(i), i);
        }
        log.Tracef("blocks to be sorted:\n");
        blocks.forEach(block -> log.Tracef("serial: [%d], label: [%s], successor: [%s]\n", block2serial.get(block), block.getLabel(), block.getSuccessors().toString()));
        int minIndex = 0;
        do {
            topo(minIndex);
            minIndex = -1;
            for (int i = blocks.size() - 1; i >= 0; i--)
                if (!mark.get(i)) minIndex = i;
        } while (minIndex != -1);
        log.Tracef("topological order of function [%s]:\n", functionName);
        sorted.forEach(block -> log.Tracef("serial: [%d], label: [%s]\n", block2serial.get(block), block.getLabel()));
        return sorted;
    }

    private final ArrayList<ASMBasicBlock> reachable = new ArrayList<>();

    public ArrayList<ASMBasicBlock> reachableBlocks() {
        reachable.clear();
        dfs(entryBlock);
        return reachable;
    }

    private void dfs(ASMBasicBlock current) {
        reachable.add(current);
        current.getSuccessors().forEach(succ -> {
            if (!reachable.contains(succ)) dfs(succ);
        });
    }

    public boolean removeUnreachableBlocks() {
        boolean ret = false;
        reachableBlocks();
        ArrayList<ASMBasicBlock> blockBackup = new ArrayList<>(blocks);
        for (ASMBasicBlock block : blockBackup) {
            if (!reachable.contains(block)) {
                blocks.remove(block);
                ret = true;
                block.getPredecessors().forEach(pred -> pred.removeSuccessor(block));
                block.getSuccessors().forEach(succ -> succ.removePredecessor(block));
            }
        }
        return ret;
    }
}

package ASM;

import ASM.Operand.ASMLabel;
import ASM.Operand.ASMPhysicalRegister;
import ASM.Operand.ASMVirtualRegister;
import IR.IRBasicBlock;
import IR.IRFunction;

import java.util.ArrayList;
import java.util.HashMap;

import static Debug.MemoLog.log;

public class ASMFunction {
    private final String functionName;
    private final ArrayList<ASMBasicBlock> blocks = new ArrayList<>();
    private final HashMap<IRBasicBlock, ASMBasicBlock> blockMap = new HashMap<>();
    private final ASMBasicBlock entryBlock;
    private final HashMap<ASMPhysicalRegister, ASMVirtualRegister> calleeSaves = new HashMap<>();
    private final ASMStackFrame stackFrame = new ASMStackFrame();
    private final ASMLabel label;

    public ASMFunction(IRFunction function) {
        this.functionName = function.getFunctionName();
        function.getBlocks().forEach(irBlock -> {
            ASMBasicBlock asmBlock = new ASMBasicBlock(this, irBlock.getLabelName());
            blockMap.put(irBlock, asmBlock);
            blocks.add(asmBlock);
        });
        log.Debugf("start request alloca for function %s\n", functionName);
        function.getEntryBlock().getAllocas().forEach(stackFrame::requestAlloca);
        this.entryBlock = blockMap.get(function.getEntryBlock());
        this.label = new ASMLabel(function.getFunctionName());
    }

    // constructor for builtin functions
    public ASMFunction(String functionName) {
        this.functionName = functionName;
        this.entryBlock = null;
        this.label = new ASMLabel(functionName);
    }

    public void addCalleeSave(ASMPhysicalRegister reg, ASMVirtualRegister calleeSave) {
        calleeSaves.put(reg, calleeSave);
    }

    public HashMap<ASMPhysicalRegister, ASMVirtualRegister> getCalleeSaves() {
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
}

package MiddleEnd;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.Instruction.*;
import IR.Operand.IRLabel;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;

import java.util.*;

import static Debug.MemoLog.log;

public class AggressiveDeadCodeEliminator implements IRFunctionPass {
    private LinkedHashMap<String, IRFunction> functions;
    private LinkedHashMap<String, IRFunction> builtins;
    private final LinkedHashSet<IRFunction> functionsWithSideEffect = new LinkedHashSet<>();
    private final LinkedHashMap<IRFunction, LinkedHashSet<IRFunction>> functionCallers = new LinkedHashMap<>();
    private final LinkedHashSet<IRInstruction> live = new LinkedHashSet<>();
    private final LinkedHashSet<IRBasicBlock> liveBlock = new LinkedHashSet<>();
    private final Queue<IRInstruction> workList = new LinkedList<>();
    private boolean changed = false;

    private void build(IRFunction function) {
        new DominatorTreeBuilder().build(function, true);
    }

    public boolean eliminate(Memory memory) {
        functions = memory.getIRModule().getFunctions();
        builtins = memory.getIRModule().getBuiltinFunctions();
        functions.values().forEach(this::build);
        initialize();
        iteration();
        functions.values().forEach(this::visit);
        if (changed) log.Infof("Program changed in adce.\n");
        return changed;
    }

    private AggressiveDeadCodeEliminator addToSideEffect(IRFunction functionWithSideEffect) {
        assert functionWithSideEffect != null;
        if (functionsWithSideEffect.contains(functionWithSideEffect)) return this;
        functionsWithSideEffect.add(functionWithSideEffect);
        functionCallers.get(functionWithSideEffect).forEach(this::addToSideEffect);
        return this;
    }

    private void markAsLiveInstruction(IRInstruction inst) {
        if (!live.contains(inst) && inst != null) {
            live.add(inst);
            liveBlock.add(inst.getParentBlock());
            workList.offer(inst);
        }
    }

    private void initialize() {
        functions.values().forEach(func -> functionCallers.put(func, new LinkedHashSet<>()));
        builtins.values().forEach(func -> functionCallers.put(func, new LinkedHashSet<>()));
        functions.values().forEach(function -> function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst instanceof IRCallInstruction) {
                IRFunction callFunc = ((IRCallInstruction) inst).getCallFunction();
                assert functionCallers.containsKey(callFunc) : callFunc + " is not in functionCallers, error inst: " + inst;
                functionCallers.get(callFunc).add(function);
            }
        })));
        addToSideEffect(builtins.get("print")).addToSideEffect(builtins.get("printInt")).addToSideEffect(builtins.get("println")).addToSideEffect(builtins.get("printlnInt"));
        addToSideEffect(builtins.get("getString")).addToSideEffect(builtins.get("getInt"));
        functions.values().forEach(function -> function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst instanceof IRCallInstruction && functionsWithSideEffect.contains(((IRCallInstruction) inst).getCallFunction()) || inst instanceof IRStoreInstruction)
                addToSideEffect(function);
        })));
        functions.values().forEach(function -> function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst instanceof IRCallInstruction && functionsWithSideEffect.contains(((IRCallInstruction) inst).getCallFunction()) || inst instanceof IRStoreInstruction || inst instanceof IRReturnInstruction)
                markAsLiveInstruction(inst);
        })));
    }

    private void iteration() {
        while (!workList.isEmpty()) {
            IRInstruction current = workList.poll();
            current.getUses().forEach(use -> {
                if (use instanceof IRLabel) markAsLiveInstruction(((IRLabel) use).belongTo().getEscapeInstruction());
                markAsLiveInstruction(use.getDef());
            });
            if (current instanceof IRPhiInstruction)
                ((IRPhiInstruction) current).forEachCandidate((block, val) -> markAsLiveInstruction(block.getEscapeInstruction()));
            if (current == current.getParentBlock().getEscapeInstruction())
                current.getParentBlock().getParentFunction().getPostDominatorFrontier().get(current.getParentBlock()).forEach(postDF -> markAsLiveInstruction(postDF.getEscapeInstruction()));
            else markAsLiveInstruction(current.getParentBlock().getEscapeInstruction());
        }
    }

    @Override
    public void visit(IRFunction function) { // eliminate
        ArrayList<IRBasicBlock> blocks = new ArrayList<>(function.getBlocks());
        blocks.forEach(block -> {
            ArrayList<IRInstruction> instructions = new ArrayList<>(block.getInstructions());
            instructions.forEach(inst -> {
                if (!live.contains(inst)) {
                    if (inst instanceof IRJumpInstruction) return;
                    changed = true;
                    if (inst instanceof IRBrInstruction) {
                        // replace br with
                        IRBasicBlock tar = block.getPostIdom();
                        // return block is live so don't need to worry tar == null
                        while (!liveBlock.contains(tar)) tar = tar.getPostIdom();
                        assert tar != null;
                        IRJumpInstruction jump = new IRJumpInstruction(block, tar);
                        block.removeSuccessor(((IRBrInstruction) inst).getThenBlock());
                        ((IRBrInstruction) inst).getThenBlock().removePredecessor(block);
                        ((IRBrInstruction) inst).getThenBlock().getPhis().forEach(phi -> phi.removeCandidate(block));
                        block.removeSuccessor(((IRBrInstruction) inst).getElseBlock());
                        ((IRBrInstruction) inst).getElseBlock().removePredecessor(block);
                        ((IRBrInstruction) inst).getElseBlock().getPhis().forEach(phi -> phi.removeCandidate(block));
                        tar.addPredecessor(block);
                        block.replaceInstructions(inst, jump);
                    } else inst.removeFromParentBlock();
                }
            });
        });
        function.removeUnreachableBlocks();
    }
}

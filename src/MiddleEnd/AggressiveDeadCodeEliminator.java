package MiddleEnd;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.Instruction.*;
import Memory.Memory;

import java.util.*;

public class AggressiveDeadCodeEliminator extends IROptimize {
    private LinkedHashMap<String, IRFunction> functions;
    private LinkedHashMap<String, IRFunction> builtins;
    private final LinkedHashSet<IRFunction> functionsWithSideEffect = new LinkedHashSet<>();
    private final LinkedHashMap<IRFunction, LinkedHashSet<IRFunction>> functionCallers = new LinkedHashMap<>();
    private final LinkedHashSet<IRInstruction> live = new LinkedHashSet<>();
    private final Queue<IRInstruction> workList = new LinkedList<>();

    private void build(IRFunction function) {
        new DominatorTreeBuilder().build(function, true);
    }

    public void eliminate(Memory memory) {
        if (doOptimize) {
            functions = memory.getIRModule().getFunctions();
            builtins = memory.getIRModule().getBuiltinFunctions();
            functions.values().forEach(this::build);
            initialize();
            iteration();
            functions.values().forEach(this::visit);
        }
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
            workList.offer(inst);
        }
    }

    private void initialize() {
        functions.values().forEach(func -> functionCallers.put(func, new LinkedHashSet<>()));
        builtins.values().forEach(func -> functionCallers.put(func, new LinkedHashSet<>()));
        functions.values().forEach(function -> function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst instanceof IRCallInstruction) functionCallers.get(((IRCallInstruction) inst).getCallFunction()).add(function);
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
            current.getUses().forEach(use -> markAsLiveInstruction(use.getDef()));
            if (current instanceof IRPhiInstruction)
                ((IRPhiInstruction) current).forEachCandidate((block, val) -> markAsLiveInstruction(block.getEscapeInstruction()));
            if (current == current.getParentBlock().getEscapeInstruction())
                current.getParentBlock().getParentFunction().getPostDominatorFrontier().get(current.getParentBlock()).forEach(postDF -> markAsLiveInstruction(postDF.getEscapeInstruction()));
            else markAsLiveInstruction(current.getParentBlock().getEscapeInstruction());
        }
    }

    @Override
    protected void visit(IRFunction function) { // eliminate
        ArrayList<IRBasicBlock> blocks = new ArrayList<>(function.getBlocks());
        blocks.forEach(block -> {
            ArrayList<IRInstruction> instructions = new ArrayList<>(block.getInstructions());
            instructions.forEach(inst -> {
                if (!live.contains(inst)) {
                    if (inst instanceof IRBrInstruction || inst instanceof IRJumpInstruction) return;
                    inst.removeFromParentBlock();
                }
            });
        });
        function.removeUnreachableBlocks();
    }
}

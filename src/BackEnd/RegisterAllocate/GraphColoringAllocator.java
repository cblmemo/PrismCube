package BackEnd.RegisterAllocate;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Instruction.*;
import ASM.Operand.*;
import org.antlr.v4.runtime.misc.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static Debug.MemoLog.log;

public class GraphColoringAllocator {
    static private final int K = ASMPhysicalRegister.getPreColoredRegisters().size();

    private final ASMFunction function;

    private final LinkedHashSet<LinkedHashSet<ASMRegister>> regSets = new LinkedHashSet<>();
    private final LinkedHashSet<LinkedHashSet<ASMMoveInstruction>> moveSets = new LinkedHashSet<>();

    private final LinkedHashSet<ASMRegister> preColored = new LinkedHashSet<>();
    private final LinkedHashSet<ASMRegister> initial = new LinkedHashSet<>();
    private final LinkedHashSet<ASMRegister> simplifyWorkList = new LinkedHashSet<>();
    private final LinkedHashSet<ASMRegister> freezeWorkList = new LinkedHashSet<>();
    private final LinkedHashSet<ASMRegister> spillWorkList = new LinkedHashSet<>();
    private final LinkedHashSet<ASMRegister> spilledNodes = new LinkedHashSet<>();
    private final LinkedHashSet<ASMRegister> coalescedNodes = new LinkedHashSet<>();
    private final LinkedHashSet<ASMRegister> coloredNodes = new LinkedHashSet<>();
    private final Stack<ASMRegister> selectStack = new Stack<>();

    private final LinkedHashSet<ASMMoveInstruction> coalescedMoves = new LinkedHashSet<>();
    private final LinkedHashSet<ASMMoveInstruction> constrainedMoves = new LinkedHashSet<>();
    private final LinkedHashSet<ASMMoveInstruction> frozenMoves = new LinkedHashSet<>();
    private final LinkedHashSet<ASMMoveInstruction> workListMoves = new LinkedHashSet<>();
    private final LinkedHashSet<ASMMoveInstruction> activeMoves = new LinkedHashSet<>();

    private final LinkedHashSet<Pair<ASMRegister, ASMRegister>> adjacentSet = new LinkedHashSet<>();
    private final LinkedHashMap<ASMRegister, LinkedHashSet<ASMRegister>> adjacentList = new LinkedHashMap<>();
    private final LinkedHashMap<ASMRegister, Integer> degree = new LinkedHashMap<>();
    private final LinkedHashMap<ASMRegister, Integer> spillCost = new LinkedHashMap<>();
    private final LinkedHashMap<ASMRegister, LinkedHashSet<ASMMoveInstruction>> moveList = new LinkedHashMap<>();
    private final LinkedHashMap<ASMRegister, ASMRegister> alias = new LinkedHashMap<>();
    private final LinkedHashMap<ASMRegister, ASMPhysicalRegister> color = new LinkedHashMap<>();
    private final LinkedHashMap<ASMRegister, Integer> memoryLocation = new LinkedHashMap<>();
    private final LinkedHashMap<ASMRegister, ASMVirtualRegister> v2vi = new LinkedHashMap<>();

    private GraphColoringAllocator addRegSet(LinkedHashSet<ASMRegister> set) {
        regSets.add(set);
        return this;
    }

    private GraphColoringAllocator addMoveSet(LinkedHashSet<ASMMoveInstruction> set) {
        moveSets.add(set);
        return this;
    }

    public GraphColoringAllocator(ASMFunction function) {
        this.function = function;
        addRegSet(preColored).addRegSet(initial).addRegSet(simplifyWorkList).addRegSet(freezeWorkList).addRegSet(spillWorkList).addRegSet(spilledNodes).addRegSet(coalescedNodes).addRegSet(coloredNodes);
        addMoveSet(coalescedMoves).addMoveSet(constrainedMoves).addMoveSet(frozenMoves).addMoveSet(workListMoves).addMoveSet(activeMoves);
    }

    private void initialize() {
        // clear all
        regSets.forEach(HashSet::clear);
        moveSets.forEach(HashSet::clear);
        selectStack.clear();
        adjacentSet.clear();
        adjacentList.clear();
        degree.clear();
        spillCost.clear();
        moveList.clear();
        alias.clear();
        color.clear();
        memoryLocation.clear();
        v2vi.clear();

        // initialize preColored and initial
        preColored.addAll(ASMPhysicalRegister.getPreColoredRegisters());
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst == null) return;
            initial.addAll(inst.getDefs());
            initial.addAll(inst.getUses());
        }));
        ASMPhysicalRegister.getPreColoredRegisters().forEach(initial::remove);

        // initialize other LinkedHashSet
        initial.forEach(reg -> {
            adjacentList.put(reg, new LinkedHashSet<>());
            degree.put(reg, 0);
            spillCost.put(reg, 0);
            moveList.put(reg, new LinkedHashSet<>());
            alias.put(reg, null);
        });
        preColored.forEach(reg -> {
            adjacentList.put(reg, new LinkedHashSet<>());
            degree.put(reg, Integer.MAX_VALUE);
            spillCost.put(reg, Integer.MAX_VALUE);
            moveList.put(reg, new LinkedHashSet<>());
            alias.put(reg, null);
            color.put(reg, (ASMPhysicalRegister) reg);
        });

        // calculate spill cost
        // spill cost = def + use
        // todo loop analysis
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst == null) return;
            inst.getDefsAndUses().forEach(reg -> spillCost.replace(reg, spillCost.get(reg) + 1));
        }));
        initial.forEach(reg -> spillCost.replace(reg, spillCost.get(reg)));
    }

    private void livenessAnalyze() {
        new LivenessAnalyzer(function).analyze();
    }

    private void addEdge(ASMRegister u, ASMRegister v) {
        if (u != v && !adjacentSet.contains(new Pair<>(u, v))) {
            adjacentSet.add(new Pair<>(u, v));
            adjacentSet.add(new Pair<>(v, u));
            if (!preColored.contains(u)) {
                adjacentList.get(u).add(v);
                degree.replace(u, degree.get(u) + 1);
            }
            if (!preColored.contains(v)) {
                adjacentList.get(v).add(u);
                degree.replace(v, degree.get(v) + 1);
            }
        }
    }

    private void buildInterferenceGraph() {
        function.getBlocks().forEach(block -> {
            LinkedHashSet<ASMRegister> live = block.getLiveOut();
            ArrayList<ASMInstruction> insts = new ArrayList<>(block.getInstructions());
            Collections.reverse(insts);
            insts.forEach(inst -> {
                if (inst == null) return;
                if (inst instanceof ASMMoveInstruction) {
                    inst.getUses().forEach(live::remove);
                    inst.getDefsAndUses().forEach(n -> moveList.get(n).add((ASMMoveInstruction) inst));
                    workListMoves.add((ASMMoveInstruction) inst);
                }
                live.addAll(inst.getDefs());
                live.forEach(l -> inst.getDefs().forEach(d -> addEdge(l, d)));
                inst.getDefs().forEach(live::remove);
                live.addAll(inst.getUses());
            });
        });
        adjacentSet.forEach(edge -> log.Tracef("[%s] --- [%s]\n", edge.a, edge.b));
    }

    private LinkedHashSet<ASMMoveInstruction> nodeMoves(ASMRegister n) {
        LinkedHashSet<ASMMoveInstruction> ret = new LinkedHashSet<>();
        ret.addAll(activeMoves);
        ret.addAll(workListMoves);
        ret.removeIf(reg -> !moveList.get(n).contains(reg));
        return ret;
    }

    private boolean moveRelated(ASMRegister n) {
        return !nodeMoves(n).isEmpty();
    }

    private void makeWorkList() {
        initial.forEach(n -> {
            if (degree.get(n) >= K) spillWorkList.add(n);
            else if (moveRelated(n)) freezeWorkList.add(n);
            else simplifyWorkList.add(n);
        });
        initial.clear();
    }

    private LinkedHashSet<ASMRegister> adjacent(ASMRegister n) {
        LinkedHashSet<ASMRegister> ret = new LinkedHashSet<>(adjacentList.get(n));
        ret.removeIf(reg -> (selectStack.contains(reg) || coalescedNodes.contains(reg)));
        return ret;
    }

    private void enableMoves(LinkedHashSet<ASMRegister> nodes) {
        nodes.forEach(n -> {
            LinkedHashSet<ASMMoveInstruction> moves = nodeMoves(n);
            moves.forEach(m -> {
                if (activeMoves.contains(m)) {
                    activeMoves.remove(m);
                    workListMoves.add(m);
                }
            });
        });
    }

    private void decrementDegree(ASMRegister m) {
        int d = degree.get(m);
        degree.replace(m, d - 1);
        if (d == K) {
            LinkedHashSet<ASMRegister> nodes = adjacent(m);
            nodes.add(m);
            enableMoves(nodes);
            spillWorkList.remove(m);
            if (moveRelated(m)) freezeWorkList.add(m);
            else simplifyWorkList.add(m);
        }
    }

    private void simplify() {
        Iterator<ASMRegister> iter = simplifyWorkList.iterator();
        ASMRegister n = iter.next();
        iter.remove();
        assert !simplifyWorkList.contains(n);
        selectStack.push(n);
        LinkedHashSet<ASMRegister> adj = adjacent(n);
        adj.forEach(this::decrementDegree);
    }

    private ASMRegister getAlias(ASMRegister n) {
        if (coalescedNodes.contains(n)) return getAlias(alias.get(n));
        return n;
    }

    private GraphColoringAllocator addWorkList(ASMRegister u) {
        if (!preColored.contains(u) && !moveRelated(u) && degree.get(u) < K) {
            freezeWorkList.remove(u);
            simplifyWorkList.add(u);
        }
        return this;
    }

    private boolean conservative(LinkedHashSet<ASMRegister> nodes) {
        AtomicInteger cnt = new AtomicInteger();
        nodes.forEach(n -> {
            if (degree.get(n) >= K) cnt.getAndIncrement();
        });
        return cnt.get() < K;
    }

    private boolean BriggsStrategy(ASMRegister u, ASMRegister v) {
        LinkedHashSet<ASMRegister> nodes = new LinkedHashSet<>();
        nodes.addAll(adjacent(u));
        nodes.addAll(adjacent(v));
        return conservative(nodes);
    }

    private boolean OK(ASMRegister t, ASMRegister r) {
        return degree.get(t) < K || preColored.contains(t) || adjacentSet.contains(new Pair<>(t, r));
    }

    private boolean GeorgeStrategy(ASMRegister u, ASMRegister v) {
        LinkedHashSet<ASMRegister> adjV = adjacent(v);
        for (ASMRegister t : adjV)
            if (!OK(t, u)) return false;
        return true;
    }

    private boolean applyStrategies(ASMRegister u, ASMRegister v) {
        // preColor registers' adjacent is too large to apply Brigg strategy
        if (preColored.contains(u)) return GeorgeStrategy(u, v);
        else return BriggsStrategy(u, v);
    }

    private void combine(ASMRegister u, ASMRegister v) {
        // execute coalesce means all nodes in simplifyWorkList have been simplified and remove from it
        // therefore u, v either in freezeWorkList or in spillWorkList
        if (freezeWorkList.contains(v)) freezeWorkList.remove(v);
        else spillWorkList.remove(v);
        coalescedNodes.add(v);
        alias.replace(v, u);
        moveList.get(u).addAll(moveList.get(v));
        LinkedHashSet<ASMRegister> adjV = adjacent(v);
        adjV.forEach(t -> {
            addEdge(t, u);
            decrementDegree(t);
        });
        if (degree.get(u) >= K && freezeWorkList.contains(u)) {
            freezeWorkList.remove(u);
            spillWorkList.add(u);
        }
    }

    private void coalesce() {
        Iterator<ASMMoveInstruction> iter = workListMoves.iterator();
        ASMMoveInstruction m = iter.next();
        iter.remove();
        ASMRegister u = getAlias(m.getRd()), v = getAlias(m.getRs());
        if (preColored.contains(v)) {
            ASMRegister temp = u;
            u = v;
            v = temp;
        }
        if (u == v) {
            coalescedMoves.remove(m);
            addWorkList(u);
        } else if (preColored.contains(v) || adjacentSet.contains(new Pair<>(u, v))) {
            constrainedMoves.add(m);
            addWorkList(u).addWorkList(v);
        } else if (applyStrategies(u, v)) {
            coalescedMoves.add(m);
            combine(u, v);
            addWorkList(u);
        } else activeMoves.add(m);
    }

    private void freezeMoves(ASMRegister u) {
        LinkedHashSet<ASMMoveInstruction> moves = nodeMoves(u);
        moves.forEach(m -> {
            ASMRegister v;
            if (getAlias(m.getRs()) == getAlias(u)) v = getAlias(m.getRd());
            else v = getAlias(m.getRs());
            activeMoves.remove(m);
            frozenMoves.add(m);
            if (nodeMoves(v).isEmpty() && degree.get(v) < K) {
                freezeWorkList.remove(v);
                simplifyWorkList.add(v);
            }
        });
    }

    private void freeze() {
        Iterator<ASMRegister> iter = freezeWorkList.iterator();
        ASMRegister u = iter.next();
        iter.remove();
        simplifyWorkList.add(u);
        freezeMoves(u);
    }

    private ASMRegister selectRegisterToSpill() {
        ASMRegister ret = null;
        int minCost = Integer.MAX_VALUE;
        for (ASMRegister n : spillWorkList) {
            int nCost = spillCost.get(n) / degree.get(n);
            if (nCost < minCost) {
                minCost = nCost;
                ret = n;
            }
        }
        assert ret != null;
        return ret;
    }

    private void selectSpill() {
        ASMRegister m = selectRegisterToSpill();
        spillWorkList.remove(m);
        simplifyWorkList.add(m);
        freezeMoves(m);
    }

    private void assignColors() {
        while (!selectStack.isEmpty()) {
            ASMRegister n = selectStack.pop();
            assert n instanceof ASMVirtualRegister;
            LinkedHashSet<ASMPhysicalRegister> okColors = new LinkedHashSet<>(ASMPhysicalRegister.getPreColoredRegisters());
            LinkedHashSet<ASMRegister> adjN = adjacent(n);
            adjN.forEach(w -> {
                ASMRegister aliasW = getAlias(w);
                if (coloredNodes.contains(aliasW) || preColored.contains(aliasW)) {
                    assert color.containsKey(aliasW);
                    okColors.remove(color.get(aliasW));
                }
            });
            if (okColors.isEmpty()) spilledNodes.add(n);
            else {
                coloredNodes.add(n);
                ASMPhysicalRegister c = okColors.iterator().next();
                color.put(n, c);
            }
        }
        coalescedNodes.forEach(n -> color.put(n, color.get(getAlias(n))));
    }

    private ArrayList<ASMInstruction> newInstructions;

    private void rewriteBlock(ASMBasicBlock block) {
        newInstructions = new ArrayList<>();
        block.getInstructions().forEach(this::rewriteInstruction);
        block.setInstructions(newInstructions);
    }

    static private boolean isValidImmediate(int imm) {
        return -2048 <= imm && imm <= 2047;
    }

    static private final ASMPhysicalRegister sp = ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp);
    static private final ASMPhysicalRegister t0 = ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.t0);

    private void rewriteInstruction(ASMInstruction inst) {
        if (inst == null) return;
        ArrayList<ASMRegister> use = new ArrayList<>(inst.getUses()), def = new ArrayList<>(inst.getDefs());
        for (ASMRegister v : use) {
            if (spilledNodes.contains(v)) {
                assert v instanceof ASMVirtualRegister;
                ASMRegister vi = v2vi.get(v);
                int loc = memoryLocation.get(v);
                if (isValidImmediate(loc)) newInstructions.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.lw, vi, new ASMAddress(sp, new ASMImmediate(loc))));
                else {
                    newInstructions.add(new ASMPseudoInstruction(ASMPseudoInstruction.InstType.li).addOperand(vi).addOperand(new ASMImmediate(loc)));
                    newInstructions.add(new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.add).addOperand(vi).addOperand(sp).addOperand(vi));
                    newInstructions.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.lw, vi, new ASMAddress(vi, null)));
                }
                inst.replaceRegister((ASMVirtualRegister) v, vi);
            }
        }
        newInstructions.add(inst);
        for (ASMRegister v : def) {
            if (spilledNodes.contains(v)) {
                assert v instanceof ASMVirtualRegister;
                ASMRegister vi = v2vi.get(v);
                int loc = memoryLocation.get(v);
                if (isValidImmediate(loc)) newInstructions.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.sw, vi, new ASMAddress(sp, new ASMImmediate(loc))));
                else {
                    newInstructions.add(new ASMPseudoInstruction(ASMPseudoInstruction.InstType.li).addOperand(vi).addOperand(new ASMImmediate(loc)));
                    newInstructions.add(new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.add).addOperand(vi).addOperand(sp).addOperand(vi));
                    newInstructions.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.sw, vi, new ASMAddress(vi, null)));
                }
                inst.replaceRegister((ASMVirtualRegister) v, vi);
            }
        }
    }

    private void rewriteProgram() {
        log.Debugf("start rewrite program.\n");
        spilledNodes.forEach(v -> {
            memoryLocation.put(v, function.getStackFrame().spillToStack());
            v2vi.put(v, new ASMVirtualRegister("spill_vi"));
        });
        function.getBlocks().forEach(this::rewriteBlock);
    }

    private void coloring() {
        initialize();
        livenessAnalyze();
        buildInterferenceGraph();
        makeWorkList();
        do {
            if (!simplifyWorkList.isEmpty()) simplify();
            else if (!workListMoves.isEmpty()) coalesce();
            else if (!freezeWorkList.isEmpty()) freeze();
            else if (!spillWorkList.isEmpty()) selectSpill();
        } while (!simplifyWorkList.isEmpty() || !workListMoves.isEmpty() || !freezeWorkList.isEmpty() || !spillWorkList.isEmpty());
        assignColors();
        if (!spilledNodes.isEmpty()) {
            rewriteProgram();
            coloring();
        }
    }

    private void aftermath() {
        color.forEach((v, c) -> log.Debugf("assign color [%s] to [%s]\n", c, v));
        ASMPhysicalRegister sp = ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp);
        ASMBasicBlock entry = function.getBlocks().get(0), escape = function.getBlocks().get(function.getBlocks().size() - 1);
        int indexOfMinusSp = entry.getInstructions().indexOf(null), indexOfPlusSp = escape.getInstructions().indexOf(null);
        ASMInstruction minusSp, plusSp;
        int frameSize = function.getStackFrame().getFrameSize();
        if (isValidImmediate(frameSize)) {
            minusSp = new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.addi).addOperand(sp).addOperand(sp).addOperand(new ASMImmediate(-frameSize));
            plusSp = new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.addi).addOperand(sp).addOperand(sp).addOperand(new ASMImmediate(frameSize));
        } else {
            entry.getInstructions().add(indexOfMinusSp++, new ASMPseudoInstruction(ASMPseudoInstruction.InstType.li).addOperand(t0).addOperand(new ASMImmediate(-frameSize)));
            minusSp = new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.add).addOperand(sp).addOperand(sp).addOperand(t0);
            escape.getInstructions().add(indexOfPlusSp++, new ASMPseudoInstruction(ASMPseudoInstruction.InstType.li).addOperand(t0).addOperand(new ASMImmediate(frameSize)));
            plusSp = new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.add).addOperand(sp).addOperand(sp).addOperand(t0);
        }
        entry.getInstructions().set(indexOfMinusSp, minusSp);
        escape.getInstructions().set(indexOfPlusSp, plusSp);
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> inst.replaceRegistersWithColor(color)));
        function.getBlocks().forEach(block -> block.getInstructions().removeIf(inst ->
                inst instanceof ASMMoveInstruction && ((ASMMoveInstruction) inst).getRd() == ((ASMMoveInstruction) inst).getRs()
        ));
    }

    public void allocate() {
        coloring();
        aftermath();
    }
}

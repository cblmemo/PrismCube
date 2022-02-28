package BackEnd.RegisterAllocate;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Instruction.*;
import ASM.Operand.*;
import BackEnd.ASMEmitter;
import org.antlr.v4.runtime.misc.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static Debug.MemoLog.log;

/**
 * This class allocate virtual register to physical register. This implementation is based on algorithm in
 * Tiger Book (Andrew W. Appel, Maia Ginsburg - Modern Compiler Implementation in C - Cambridge University Press (1998)).
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class GraphColoringAllocator {
    static private final int K = ASMPhysicalRegister.getPreColoredRegisters().size();

    private final ASMFunction function;

    private final LinkedHashSet<ASMRegister> physicalRegisters = new LinkedHashSet<>(ASMPhysicalRegister.getPhysicalRegisters());
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

    public GraphColoringAllocator(ASMFunction function) {
        this.function = function;
    }

    private void clear() {
        log.Tracef("start clear.\n");
        preColored.clear();
        initial.clear();
        simplifyWorkList.clear();
        freezeWorkList.clear();
        spillWorkList.clear();
        spilledNodes.clear();
        coalescedNodes.clear();
        coloredNodes.clear();
        coalescedMoves.clear();
        constrainedMoves.clear();
        frozenMoves.clear();
        workListMoves.clear();
        activeMoves.clear();
        selectStack.clear();
        adjacentSet.clear();
        adjacentList.clear();
        degree.clear();
        spillCost.clear();
        moveList.clear();
        alias.clear();
        color.clear();
        memoryLocation.clear();
        log.Tracef("clear finished.\n");
    }

    private void initialize() {
        // clear all
        clear();

        // initialize preColored and initial
        preColored.addAll(ASMPhysicalRegister.getPreColoredRegisters());
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst == null) return;
            initial.addAll(inst.getDefs());
            initial.addAll(inst.getUses());
        }));
        ASMPhysicalRegister.getPreColoredRegisters().forEach(initial::remove);
        log.Tracef("initial registers (%d): %s\n", initial.size(), initial.toString());
        log.Tracef("preColored registers (%d): %s\n", preColored.size(), preColored.toString());

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
        // spill cost = def + use / degree
        // todo loop analysis
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst == null) return;
            inst.getDefsAndUses().forEach(reg -> {
                if (!preColored.contains(reg)) spillCost.replace(reg, spillCost.get(reg) + 1);
            });
        }));
    }

    private void livenessAnalyze() {
        new LivenessAnalyzer(function).analyze();
    }

    private boolean addEdge(ASMRegister u, ASMRegister v) {
        boolean ret = false;
        if (u != v && !adjacentSet.contains(new Pair<>(u, v))) {
            log.Tracef("add an edge between [%s] and [%s]\n", u, v);
            adjacentSet.add(new Pair<>(u, v));
            adjacentSet.add(new Pair<>(v, u));
            if (!preColored.contains(u)) {
                adjacentList.get(u).add(v);
                degree.replace(u, degree.get(u) + 1);
                ret = true;
            }
            if (!preColored.contains(v)) {
                adjacentList.get(v).add(u);
                degree.replace(v, degree.get(v) + 1);
            }
        }
        return ret;
    }

    private void buildInterferenceGraph() {
        function.getBlocks().forEach(block -> {
            LinkedHashSet<ASMRegister> live = block.getLiveOut();
            ArrayList<ASMInstruction> instructions = new ArrayList<>(block.getInstructions());
            Collections.reverse(instructions);
            instructions.forEach(inst -> {
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
        StringBuilder graphString = new StringBuilder();
        graphString.append("------------------------------------------------------------------------------------------\n");
        graphString.append("degree:\n").append(degree).append("\n");
        graphString.append(String.format("Interference Graph (%d):\n", adjacentSet.size()));
        adjacentSet.forEach(edge -> graphString.append(String.format("edge: [%s]" + " ".repeat(Integer.max(1, 40 - edge.a.toString().length())) + "[%s]\n", edge.a, edge.b)));
        graphString.append("------------------------------------------------------------------------------------------\n");
        log.Tracef("%s", graphString.toString());
    }

    private LinkedHashSet<ASMMoveInstruction> nodeMoves(ASMRegister n) {
        LinkedHashSet<ASMMoveInstruction> ret = new LinkedHashSet<>();
        ret.addAll(activeMoves);
        ret.addAll(workListMoves);
        ret.retainAll(moveList.get(n));
        return ret;
    }

    private boolean moveRelated(ASMRegister n) {
        return !nodeMoves(n).isEmpty();
    }

    private void makeWorkList() {
        log.Tracef("start make work list.\n");
        initial.forEach(n -> {
            if (degree.get(n) >= K) spillWorkList.add(n);
            else if (moveRelated(n)) freezeWorkList.add(n);
            else simplifyWorkList.add(n);
            if (degree.get(n) >= K) log.Tracef("add [%s] to spillWorkList\n", n);
            else if (moveRelated(n)) log.Tracef("add [%s] to freezeWorkList\n", n);
            else log.Tracef("add [%s] to simplifyWorkList\n", n);
        });
        log.Tracef("make work list finished.\n");
    }

    private LinkedHashSet<ASMRegister> adjacent(ASMRegister n) {
        assert n instanceof ASMVirtualRegister : "query adjacentList for physical register " + n;
        assert adjacentList.containsKey(n) : n + " is not in adjacentList";
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
        log.Tracef("decrement [%s]'s degree to %d\n", m, d - 1);
        if (d == K) {
            LinkedHashSet<ASMRegister> nodes = adjacent(m);
            nodes.add(m);
            enableMoves(nodes);
            assert spillWorkList.contains(m) : m + " is not in spillWorkList";
            spillWorkList.remove(m);
            log.Tracef("remove [%s] to spillWorkList due to decrement degree\n", m);
            if (moveRelated(m)) freezeWorkList.add(m);
            else simplifyWorkList.add(m);
        }
    }

    private void decrementDegreeWithoutCheck(ASMRegister m) {
        int d = degree.get(m);
        degree.replace(m, d - 1);
        log.Tracef("decrement [%s]'s degree to %d\n", m, d - 1);
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
        if (!physicalRegisters.contains(u) && !moveRelated(u) && degree.get(u) < K) {
            assert freezeWorkList.contains(u) : u + " is not in freezeWorkList, degree: " + degree.get(u) + ", spillWorkList: " + spillWorkList.contains(u);
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
        assert u instanceof ASMVirtualRegister && v instanceof ASMVirtualRegister : String.format("use Brigg to preColored register [%s] && [%s]", u, v);
        LinkedHashSet<ASMRegister> nodes = new LinkedHashSet<>();
        nodes.addAll(adjacent(u));
        nodes.addAll(adjacent(v));
        return conservative(nodes);
    }

    private boolean OK(ASMRegister t, ASMRegister r) {
        return degree.get(t) < K || preColored.contains(t) || adjacentSet.contains(new Pair<>(t, r));
    }

    private boolean GeorgeStrategy(ASMRegister u, ASMRegister v) {
        assert v instanceof ASMVirtualRegister : String.format("use George to two preColored register [%s] && [%s]", u, v);
        LinkedHashSet<ASMRegister> adjV = adjacent(v);
        for (ASMRegister t : adjV)
            if (!OK(t, u)) return false;
        return true;
    }

    private boolean applyStrategies(ASMRegister u, ASMRegister v) {
        // preColor registers' adjacent is too large to apply Brigg strategy
        // if there is a preColored register in u, v, then it will be swapped to u
        // therefore preColored.contains(u) means there is a preColored register in u, v
        boolean ret;
        if (preColored.contains(u)) ret = GeorgeStrategy(u, v);
        else ret = BriggsStrategy(u, v);
        if (ret) log.Tracef("applying %s strategy success.\n", preColored.contains(u) ? "George" : "Briggs");
        return ret;
    }

    private void combine(ASMRegister u, ASMRegister v) {
        log.Tracef("combine u:[%s] with v:[%s]\n", u, v);
        // execute coalesce means all nodes in simplifyWorkList have been simplified and remove from it
        // therefore u, v either in freezeWorkList or in spillWorkList
        if (freezeWorkList.contains(v)) freezeWorkList.remove(v);
        else spillWorkList.remove(v);
        coalescedNodes.add(v);
        alias.replace(v, u);
        moveList.get(u).addAll(moveList.get(v));
        LinkedHashSet<ASMRegister> adjV = adjacent(v);
        log.Tracef("adjV: %s\n", adjV.toString());
        adjV.forEach(t -> {
            if (addEdge(t, u)) decrementDegreeWithoutCheck(t);
        });
        if (degree.get(u) >= K && freezeWorkList.contains(u)) {
            log.Tracef("transfer [%s] from freezeWorkList to spillWorkList due to combine node\n", u);
            freezeWorkList.remove(u);
            spillWorkList.add(u);
        }
    }

    private void coalesce() {
        Iterator<ASMMoveInstruction> iter = workListMoves.iterator();
        ASMMoveInstruction m = iter.next();
        iter.remove();
        assert !workListMoves.contains(m);
        ASMRegister u = getAlias(m.getRd()), v = getAlias(m.getRs());
        if (preColored.contains(v)) {
            ASMRegister temp = u;
            u = v;
            v = temp;
        }
        if (u == v) {
            coalescedMoves.add(m);
            addWorkList(u);
        } else if (physicalRegisters.contains(v) || adjacentSet.contains(new Pair<>(u, v))) {
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
        // choose a move related node, freeze all related move and add it to simplifyWorkList
        // notice all node that degree >= K is in spillWorkList
        Iterator<ASMRegister> iter = freezeWorkList.iterator();
        ASMRegister u = iter.next();
        iter.remove();
        assert !freezeWorkList.contains(u);
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
        assert !preColored.contains(ret) : "pre colored register cannot spill to stack";
        return ret;
    }

    private void selectSpill() {
        ASMRegister m = selectRegisterToSpill();
        log.Tracef("select [%s] to spill\n", m);
        spillWorkList.remove(m);
        simplifyWorkList.add(m);
        freezeMoves(m);
    }

    private void assignColors() {
        while (!selectStack.isEmpty()) {
            ASMRegister n = selectStack.pop();
            assert n instanceof ASMVirtualRegister;
            LinkedHashSet<ASMPhysicalRegister> okColors = new LinkedHashSet<>(ASMPhysicalRegister.getPreColoredRegisters());
            adjacentList.get(n).forEach(w -> {
                ASMRegister aliasW = getAlias(w);
                if (coloredNodes.contains(aliasW) || preColored.contains(aliasW)) {
                    assert color.containsKey(aliasW) : aliasW + " is not in <color>";
                    okColors.remove(color.get(aliasW));
                }
            });
            if (okColors.isEmpty()) spilledNodes.add(n);
            else {
                coloredNodes.add(n);
                ASMPhysicalRegister c = okColors.iterator().next();
                color.put(n, c);
                log.Tracef("temporary assign color [%s] to [%s]\n", c, n);
            }
        }
        coalescedNodes.forEach(n -> color.put(n, color.get(getAlias(n))));
    }

    static private boolean isValidImmediate(int imm) {
        return -2048 <= imm && imm <= 2047;
    }

    static private final ASMPhysicalRegister sp = ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp);
    static private final ASMPhysicalRegister t0 = ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.t0);

    private void replaceRegisterInInstruction(ASMInstruction inst, ASMRegister v, ASMMemoryInstruction.InstType type) {
        if (spilledNodes.contains(v)) {
            assert v instanceof ASMVirtualRegister;
            ASMVirtualRegister vi = new ASMVirtualRegister("spill");
            int loc = memoryLocation.get(v);
            if (isValidImmediate(loc)) newInstructions.add(new ASMMemoryInstruction(block, type, vi, new ASMAddress(sp, new ASMImmediate(loc))));
            else {
                ASMVirtualRegister location = new ASMVirtualRegister("mem_loc");
                newInstructions.add(new ASMPseudoInstruction(block, ASMPseudoInstruction.InstType.li).addOperand(location).addOperand(new ASMImmediate(loc)));
                newInstructions.add(new ASMArithmeticInstruction(block, ASMArithmeticInstruction.InstType.add).addOperand(location).addOperand(sp).addOperand(location));
                newInstructions.add(new ASMMemoryInstruction(block, type, vi, new ASMAddress(location, null)));
            }
            inst.replaceRegister((ASMVirtualRegister) v, vi);
        }
    }

    private void rewriteInstruction(ASMInstruction inst) {
        if (inst == null) { // plus sp or minus sp
            newInstructions.add(null);
            return;
        }
        ArrayList<ASMRegister> use = new ArrayList<>(inst.getUses()), def = new ArrayList<>(inst.getDefs());
        for (ASMRegister v : use) replaceRegisterInInstruction(inst, v, ASMMemoryInstruction.InstType.lw);
        newInstructions.add(inst);
        for (ASMRegister v : def) replaceRegisterInInstruction(inst, v, ASMMemoryInstruction.InstType.sw);
    }

    private ASMBasicBlock block;
    private ArrayList<ASMInstruction> newInstructions;

    private void rewriteBlock(ASMBasicBlock block) {
        newInstructions = new ArrayList<>();
        this.block = block;
        block.getInstructions().forEach(this::rewriteInstruction);
        block.setInstructions(newInstructions);
    }

    private void rewriteProgram() {
        log.Tracef("start rewrite program.\n");
        log.Tracef("spilledNodes: %s\n", spilledNodes.toString());
        spilledNodes.forEach(v -> {
            int loc = function.getStackFrame().spillToStack();
            memoryLocation.put(v, loc);
            log.Tracef("spill [%s] to (%d)sp.\n", v, loc);
        });
        function.getBlocks().forEach(this::rewriteBlock);
        log.Tracef("rewrite program finished.\n");
    }

    private void logCurrentFunction(String message) {
        log.Tracef("------------------------------------------------------------------------------------------\n");
        log.Tracef(message);
        log.Tracef("%s", new ASMEmitter().emitFunctionToString(function));
        log.Tracef("------------------------------------------------------------------------------------------\n");
    }

    private void coloring() {
        logCurrentFunction("function to be allocated:\n");
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
        color.forEach((v, c) -> {
            if (v instanceof ASMPhysicalRegister) assert v == c : "physical register assigned to a color that not itself";
            else log.Tracef("assign color [%s] to [%s]\n", c, v);
        });
        ASMBasicBlock entry = function.getBlocks().get(0), escape = function.getBlocks().get(function.getBlocks().size() - 1);
        int indexOfMinusSp = entry.getInstructions().indexOf(null), indexOfPlusSp = escape.getInstructions().indexOf(null);
        ASMInstruction minusSp, plusSp;
        int frameSize = function.getStackFrame().getFrameSize();
        if (isValidImmediate(frameSize)) {
            minusSp = new ASMArithmeticInstruction(entry, ASMArithmeticInstruction.InstType.addi).addOperand(sp).addOperand(sp).addOperand(new ASMImmediate(-frameSize));
            plusSp = new ASMArithmeticInstruction(escape, ASMArithmeticInstruction.InstType.addi).addOperand(sp).addOperand(sp).addOperand(new ASMImmediate(frameSize));
        } else {
            entry.getInstructions().add(indexOfMinusSp++, new ASMPseudoInstruction(entry, ASMPseudoInstruction.InstType.li).addOperand(t0).addOperand(new ASMImmediate(-frameSize)));
            minusSp = new ASMArithmeticInstruction(entry, ASMArithmeticInstruction.InstType.add).addOperand(sp).addOperand(sp).addOperand(t0);
            escape.getInstructions().add(indexOfPlusSp++, new ASMPseudoInstruction(escape, ASMPseudoInstruction.InstType.li).addOperand(t0).addOperand(new ASMImmediate(frameSize)));
            plusSp = new ASMArithmeticInstruction(escape, ASMArithmeticInstruction.InstType.add).addOperand(sp).addOperand(sp).addOperand(t0);
        }
        entry.getInstructions().set(indexOfMinusSp, minusSp);
        escape.getInstructions().set(indexOfPlusSp, plusSp);
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> inst.replaceRegistersWithColor(color)));
        function.getBlocks().forEach(block -> block.getInstructions().removeIf(inst -> inst instanceof ASMMoveInstruction && ((ASMMoveInstruction) inst).eliminable()));
        logCurrentFunction("function after allocate:\n");
    }

    public void allocate() {
        log.Infof("start graph coloring register allocate for function [%s].\n", function.getFunctionName());
        coloring();
        aftermath();
        log.Infof("graph coloring register allocate for function [%s] finished.\n", function.getFunctionName());
    }
}

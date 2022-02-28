package MiddleEnd.IROptimize;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.Instruction.*;
import IR.Operand.IRConstInt;
import IR.Operand.IRConstNumber;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;
import Utility.error.OptimizeError;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static Debug.MemoLog.log;

public class SparseConditionalConstantPropagator implements IRFunctionPass {
    private IRFunction function;
    private final LinkedHashMap<IRRegister, LatticeType> constType = new LinkedHashMap<>();
    private final LinkedHashMap<IRRegister, IRConstNumber> constValue = new LinkedHashMap<>();
    private final LinkedHashSet<IRBasicBlock> executed = new LinkedHashSet<>();
    private final Queue<IRBasicBlock> blockWorkList = new LinkedList<>();
    private final Queue<IRRegister> variableWorkList = new LinkedList<>();
    private boolean changed = false;
    private boolean functionChanged = false;

    private enum LatticeType {
        undefined, defined, overdefined
    }

    public boolean propagate(Memory memory) {
        memory.getIRModule().getFunctions().values().forEach(this::visit);
        if (changed) log.Infof("Program changed in sccp.\n");
        return changed;
    }

    private void addToBlockList(IRBasicBlock block) {
        if (!executed.contains(block)) {
            executed.add(block);
            blockWorkList.offer(block);
        }
    }

    private IRConstNumber extractConst(IROperand operand) {
        if (operand instanceof IRConstNumber) return (IRConstNumber) operand;
        if (operand instanceof IRRegister) {
            if (constType.get((IRRegister) operand) != LatticeType.defined) return null;
            return constValue.getOrDefault((IRRegister) operand, null);
        }
        return null;
    }

    private void putValue(IRRegister reg, int val) {
        constValue.put(reg, new IRConstInt(null, val));
    }

    private void initialize() {
        constType.clear();
        constValue.clear();
        executed.clear();
        blockWorkList.clear();
        variableWorkList.clear();
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst.getDef() != null) constType.put(inst.getDef(), LatticeType.undefined);
        }));
    }

    @Override
    public void visit(IRFunction function) {
        this.function = function;
        functionChanged = false;
        initialize();
        function.getParameters().forEach(para -> constType.put(para, LatticeType.overdefined));
        addToBlockList(function.getEntryBlock());
        while (!variableWorkList.isEmpty() || !blockWorkList.isEmpty()) {
            if (!variableWorkList.isEmpty()) {
                IRRegister cur = variableWorkList.poll();
                cur.getUsers().forEach(this::propagateInstruction);
            }
            if (!blockWorkList.isEmpty()) {
                IRBasicBlock cur = blockWorkList.poll();
                // any block in worklist must have been added to executed, therefore all instructions inside will be visited
                cur.getInstructions().forEach(this::propagateInstruction);
                cur.getSuccessors().forEach(succ -> succ.getPhis().forEach(this::propagateInstruction));
            }
        }
        function.getBlocks().forEach(block -> {
            ArrayList<IRInstruction> insts = new ArrayList<>(block.getInstructions());
            insts.forEach(inst -> {
                if (constType.get(inst.getDef()) == LatticeType.defined) {
                    IRConstNumber constVal = constValue.get(inst.getDef());
                    IROperand val = inst.getDef().getIRType().getCorrespondingConstOperandType().cloneFromIntValue(constVal.getIntValue());
                    inst.replaceAllUseWithValue(val);
                    inst.removeFromParentBlock();
                    functionChanged = true;
                } else {
                    if (inst instanceof IRBrInstruction) {
                        IRConstNumber num = extractConst(((IRBrInstruction) inst).getCondition());
                        if (num != null) {
                            IRJumpInstruction jump;
                            if (num.getIntValue() == 0) {
                                jump = new IRJumpInstruction(block, ((IRBrInstruction) inst).getElseBlock());
                                block.removeSuccessor(((IRBrInstruction) inst).getThenBlock());
                                ((IRBrInstruction) inst).getThenBlock().removePredecessor(block);
                                ((IRBrInstruction) inst).getThenBlock().getPhis().forEach(phi -> phi.removeCandidate(block));
                            } else {
                                jump = new IRJumpInstruction(block, ((IRBrInstruction) inst).getThenBlock());
                                block.removeSuccessor(((IRBrInstruction) inst).getElseBlock());
                                ((IRBrInstruction) inst).getElseBlock().removePredecessor(block);
                                ((IRBrInstruction) inst).getElseBlock().getPhis().forEach(phi -> phi.removeCandidate(block));
                            }
                            block.replaceInstructions(inst, jump);
                        }
                    }
                    if (inst instanceof IRPhiInstruction) {
                        ArrayList<IRBasicBlock> candidates = new ArrayList<>(((IRPhiInstruction) inst).getBlocks());
                        candidates.forEach(c -> {
                            if (!executed.contains(c) && ((IRPhiInstruction) inst).getBlocks().size() > 1) ((IRPhiInstruction) inst).removeCandidate(c);
                        });
                        if (((IRPhiInstruction) inst).getBlocks().size() == 1) {
                            inst.replaceAllUseWithValue(((IRPhiInstruction) inst).getValues().get(0));
                            inst.removeFromParentBlock();
                        }
                    }
                }
            });
        });
        changed |= functionChanged;
    }

    private void propagateInstruction(IRInstruction inst) {
        if (!executed.contains(inst.getParentBlock())) return;
        LatticeType oldType = constType.get(inst.getDef());
        if (inst instanceof IRAllocaInstruction) propagateAlloca((IRAllocaInstruction) inst);
        else if (inst instanceof IRBinaryInstruction) propagateBinary((IRBinaryInstruction) inst);
        else if (inst instanceof IRBitcastInstruction) propagateBitcast((IRBitcastInstruction) inst);
        else if (inst instanceof IRBrInstruction) propagateBr((IRBrInstruction) inst);
        else if (inst instanceof IRCallInstruction) propagateCall((IRCallInstruction) inst);
        else if (inst instanceof IRGetelementptrInstruction) propagateGetElementPtr((IRGetelementptrInstruction) inst);
        else if (inst instanceof IRIcmpInstruction) propagateIcmp((IRIcmpInstruction) inst);
        else if (inst instanceof IRJumpInstruction) propagateJump((IRJumpInstruction) inst);
        else if (inst instanceof IRLoadInstruction) propagateLoad((IRLoadInstruction) inst);
        else if (inst instanceof IRMoveInstruction) propagateMove((IRMoveInstruction) inst);
        else if (inst instanceof IRPhiInstruction) propagatePhi((IRPhiInstruction) inst);
        else if (inst instanceof IRReturnInstruction) propagateReturn((IRReturnInstruction) inst);
        else if (inst instanceof IRStoreInstruction) propagateStore((IRStoreInstruction) inst);
        else if (inst instanceof IRTruncInstruction) propagateTrunc((IRTruncInstruction) inst);
        else if (inst instanceof IRZextInstruction) propagateZext((IRZextInstruction) inst);
        else throw new OptimizeError("unexpected instruction type of inst: " + inst);
        if (inst.getDef() != null && oldType != constType.get(inst.getDef())) variableWorkList.offer(inst.getDef());
    }

    private void moveTo(IRRegister res, IROperand operand) {
        IRConstNumber rhs = extractConst(operand);
        if (rhs == null) {
            constType.put(res, LatticeType.overdefined);
            return;
        }
        constType.put(res, LatticeType.defined);
        putValue(res, rhs.getIntValue());
    }

    private void propagateAlloca(IRAllocaInstruction inst) {
        throw new OptimizeError("unexpected alloca in SCCP");
    }

    private void propagateBinary(IRBinaryInstruction inst) {
        IRConstNumber lhs = extractConst(inst.getLhs()), rhs = extractConst(inst.getRhs());
        IRRegister res = inst.getResultRegister();
        if (lhs == null || rhs == null) {
            if (lhs != null || rhs != null) {
                int val = lhs == null ? rhs.getIntValue() : lhs.getIntValue();
                if (inst.isLogicBinary()) {
                    if (val == 0 && Objects.equals(inst.getOp(), "and")) {
                        constType.put(res, LatticeType.defined);
                        putValue(res, 0);
                    } else if (val == 1 && Objects.equals(inst.getOp(), "or")) {
                        constType.put(res, LatticeType.defined);
                        putValue(res, 1);
                    } else constType.put(res, LatticeType.overdefined);
                } else if (Objects.equals(inst.getOp(), "sdiv")) {
                    if (val == 0) {
                        constType.put(res, LatticeType.defined);
                        putValue(res, 0);
                    } else constType.put(res, LatticeType.overdefined);
                } else constType.put(res, LatticeType.overdefined);
            } else constType.put(res, LatticeType.overdefined);
            return;
        }
        int lhsVal = lhs.getIntValue(), rhsVal = rhs.getIntValue();
        if (rhsVal == 0 && Objects.equals(inst.getOp(), "sdiv")) {
            constType.put(res, LatticeType.overdefined);
            return;
        }
        constType.put(res, LatticeType.defined);
        switch (inst.getOp()) {
            case "add" -> putValue(res, lhsVal + rhsVal);
            case "sub nsw" -> putValue(res, lhsVal - rhsVal);
            case "mul" -> putValue(res, lhsVal * rhsVal);
            case "sdiv" -> putValue(res, lhsVal / rhsVal);
            case "srem" -> putValue(res, lhsVal % rhsVal);
            case "shl nsw" -> putValue(res, lhsVal << rhsVal);
            case "ashr" -> putValue(res, lhsVal >> rhsVal);
            case "and" -> putValue(res, lhsVal & rhsVal);
            case "xor" -> putValue(res, lhsVal ^ rhsVal);
            case "or" -> putValue(res, lhsVal | rhsVal);
            default -> throw new OptimizeError("unexpected binary op " + inst.getOp());
        }
    }

    private void propagateBitcast(IRBitcastInstruction inst) {
        // I think only rhs == null could be propagated
        moveTo(inst.getResultRegister(), inst.getPtrValue());
    }

    private void propagateBr(IRBrInstruction inst) {
        IRConstNumber condition = extractConst(inst.getCondition());
        if (condition == null) {
            addToBlockList(inst.getThenBlock());
            addToBlockList(inst.getElseBlock());
        } else {
            if (condition.getIntValue() != 0) addToBlockList(inst.getThenBlock());
            else addToBlockList(inst.getElseBlock());
        }
    }

    private void propagateCall(IRCallInstruction inst) {
        if (inst.haveReturnValue()) {
            constType.put(inst.getResultRegister(), LatticeType.overdefined);
        }
    }

    private void propagateGetElementPtr(IRGetelementptrInstruction inst) {
        constType.put(inst.getResultRegister(), LatticeType.overdefined);
    }

    private boolean containsEqual(String op) {
        return Objects.equals(op, "sle") || Objects.equals(op, "sge") || Objects.equals(op, "seq");
    }

    private void propagateIcmp(IRIcmpInstruction inst) {
        IRConstNumber lhs = extractConst(inst.getLhs()), rhs = extractConst(inst.getRhs());
        IRRegister res = inst.getResultRegister();
        if (lhs == null || rhs == null) {
            if (inst.getLhs() == inst.getRhs()) {
                if (containsEqual(inst.getOp())) putValue(res, 1);
                else putValue(res, 0);
                constType.put(res, LatticeType.defined);
            } else constType.put(res, LatticeType.overdefined);
            return;
        }
        int lhsVal = lhs.getIntValue(), rhsVal = rhs.getIntValue();
        constType.put(res, LatticeType.defined);
        switch (inst.getOp()) {
            case "slt" -> putValue(res, lhsVal < rhsVal ? 1 : 0);
            case "sle" -> putValue(res, lhsVal <= rhsVal ? 1 : 0);
            case "sgt" -> putValue(res, lhsVal > rhsVal ? 1 : 0);
            case "sge" -> putValue(res, lhsVal >= rhsVal ? 1 : 0);
            case "eq" -> putValue(res, lhsVal == rhsVal ? 1 : 0);
            case "ne" -> putValue(res, lhsVal != rhsVal ? 1 : 0);
            default -> throw new OptimizeError("unexpected icmp op " + inst.getOp());
        }
    }

    private void propagateJump(IRJumpInstruction inst) {
        addToBlockList(inst.getTargetBlock());
    }

    private void propagateLoad(IRLoadInstruction inst) {
        constType.put(inst.getResultRegister(), LatticeType.overdefined);
    }

    private void propagateMove(IRMoveInstruction inst) {
        moveTo(inst.getResultRegister(), inst.getValue());
    }

    private void propagatePhi(IRPhiInstruction inst) {
        AtomicReference<LatticeType> type = new AtomicReference<>(LatticeType.undefined);
        AtomicInteger phiValue = new AtomicInteger(-1);
        inst.forEachCandidate((block, value) -> {
            if (executed.contains(block)) {
                IRConstNumber num = extractConst(value);
                if (num == null) {
                    if (!(value instanceof IRRegister && constType.getOrDefault((IRRegister) value, null) == LatticeType.undefined))
                        type.set(LatticeType.overdefined);
                } else {
                    if (type.get() == LatticeType.undefined) {
                        type.set(LatticeType.defined);
                        phiValue.set(num.getIntValue());
                    } else if (phiValue.get() != num.getIntValue()) type.set(LatticeType.overdefined);
                }
            }
        });
        switch (type.get()) {
            case overdefined -> constType.put(inst.getResultRegister(), LatticeType.overdefined);
            case defined -> {
                constType.put(inst.getResultRegister(), LatticeType.defined);
                putValue(inst.getResultRegister(), phiValue.get());
            }
        }
    }

    private void propagateReturn(IRReturnInstruction inst) {
        // no def
    }

    private void propagateStore(IRStoreInstruction inst) {
        // no def
    }

    private void propagateTrunc(IRTruncInstruction inst) {
        moveTo(inst.getResultRegister(), inst.getTruncTarget());
    }

    private void propagateZext(IRZextInstruction inst) {
        moveTo(inst.getResultRegister(), inst.getZextTarget());
    }
}

package MiddleEnd.IROptimize;

import IR.IRBasicBlock;
import IR.IRFunction;
import IR.IRModule;
import IR.Instruction.IRBinaryInstruction;
import IR.Instruction.IRPhiInstruction;
import IR.Operand.IRConstInt;
import IR.Operand.IRConstNumber;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;
import MiddleEnd.Utils.IRLoop;
import MiddleEnd.Utils.LoopExtractor;

import java.util.LinkedHashMap;
import java.util.Objects;

import static Debug.MemoLog.log;

/**
 * This class implements strength reduction to have a better performance on loop.
 * <br>Algorithm: Tiger Book, Chapter 18.3.2
 *
 * @author rainy memory
 * @version 1.0.0
 */

public class LoopOptimizer implements IRFunctionPass {
    private static class BasicInductionVariable {
        private final int initVal, stepVal;
        private final IRBasicBlock initBlock, stepBlock;

        public BasicInductionVariable(int initVal, int stepVal, IRBasicBlock initBlock, IRBasicBlock stepBlock) {
            this.initVal = initVal;
            this.stepVal = stepVal;
            this.initBlock = initBlock;
            this.stepBlock = stepBlock;
        }

        public int getInitVal() {
            return initVal;
        }

        public int getStepVal() {
            return stepVal;
        }

        public IRBasicBlock getInitBlock() {
            return initBlock;
        }

        public IRBasicBlock getStepBlock() {
            return stepBlock;
        }
    }

    // j = (i, a, b) -> j = a + b * i
    // only consider case of a == 0
    private static class DerivedInductionVariable {
        private final IRBinaryInstruction parentInstruction;
        private final BasicInductionVariable family;
        private final int a, b;

        public DerivedInductionVariable(IRBinaryInstruction parentInstruction, BasicInductionVariable family, int a, int b) {
            this.parentInstruction = parentInstruction;
            this.family = family;
            this.a = a;
            this.b = b;
        }

        public IRBinaryInstruction getParentInstruction() {
            return parentInstruction;
        }

        public BasicInductionVariable getFamily() {
            return family;
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }
    }

    private boolean changed = false;

    private IRLoop loop;
    private final LinkedHashMap<IRRegister, BasicInductionVariable> basicInductionVariable = new LinkedHashMap<>();
    private final LinkedHashMap<IRRegister, DerivedInductionVariable> derivedInductionVariable = new LinkedHashMap<>();

    public boolean doi(Memory memory) {
        memory.getIRModule().getFunctions().values().forEach(this::visit);
        if (changed) log.Infof("Program changed in loop.\n");
        return changed;
    }

    // since we invoke this class only in SSA form, all "there is only one definition of xxx" statement is true
    private void inductionVariableAnalysis() {
        basicInductionVariable.clear();
        derivedInductionVariable.clear();
        // find basic induction variable
        // biv = phi [initVal, initBlock], [stepVal, stepBlock] in header node
        loop.getHeader().getPhis().forEach(phi -> {
            if (phi.getBlocks().size() != 2) return;
            IRBasicBlock block0 = phi.getBlocks().get(0), block1 = phi.getBlocks().get(1);
            IROperand val0 = phi.getValues().get(0), val1 = phi.getValues().get(1);
            IRBasicBlock initBlock, stepBlock;
            IROperand initVal, stepVal;
            if (loop.containsNode(block0) && !loop.containsNode(block1)) {
                initBlock = block1;
                initVal = val1;
                stepBlock = block0;
                stepVal = val0;
            } else if (!loop.containsNode(block0) && loop.containsNode(block1)) {
                initBlock = block0;
                initVal = val0;
                stepBlock = block1;
                stepVal = val1;
            } else return;
            if (!(initVal instanceof IRConstNumber) || !(stepVal instanceof IRRegister)) return;
            assert stepVal.getDef() != null : "step val [" + stepVal + "] has no def";
            BasicInductionVariable biv;
            if (stepVal.getDef() instanceof IRBinaryInstruction) {
                Integer cInt = ((IRBinaryInstruction) stepVal.getDef()).getConstOtherThan(phi.getResultRegister());
                if (cInt == null) return;
                int init = ((IRConstNumber) initVal).getIntValue(), c = cInt;
                switch (((IRBinaryInstruction) stepVal.getDef()).getOp()) {
                    case "add" -> biv = new BasicInductionVariable(init, c, initBlock, stepBlock);
                    case "sub nsw" -> biv = new BasicInductionVariable(init, -c, initBlock, stepBlock);
                    default -> biv = null;
                }
                assert phi.getResultRegister().getIRType().isInt() : phi;
                if (biv != null) basicInductionVariable.put(phi.getResultRegister(), biv);
            }
        });
        // find derived induction variable
        // only consider mul for strengthReduction
        loop.forEachLoopInstruction(inst -> {
            if (inst instanceof IRBinaryInstruction && Objects.equals(((IRBinaryInstruction) inst).getOp(), "mul")) {
                IROperand lhs = ((IRBinaryInstruction) inst).getLhs(), rhs = ((IRBinaryInstruction) inst).getRhs();
                if (lhs instanceof IRRegister && basicInductionVariable.containsKey(lhs) && loop.invariantInThisLoop(rhs)) {
                    BasicInductionVariable family = basicInductionVariable.get(lhs);
                    int b = loop.extractInvariantOperand(rhs);
                    derivedInductionVariable.put(((IRBinaryInstruction) inst).getResultRegister(), new DerivedInductionVariable((IRBinaryInstruction) inst, family, 0, b));
                } else if (loop.invariantInThisLoop(lhs) && rhs instanceof IRRegister && basicInductionVariable.containsKey(rhs)) {
                    BasicInductionVariable family = basicInductionVariable.get(rhs);
                    int b = loop.extractInvariantOperand(lhs);
                    derivedInductionVariable.put(((IRBinaryInstruction) inst).getResultRegister(), new DerivedInductionVariable((IRBinaryInstruction) inst, family, 0, b));
                }
            }
        });
    }

    private void strengthReduction() {
        // i = phi [initVal, initBlock], [stepVal, stepBlock]
        // for j = a + b * i insert
        // j' = phi [b * initVal, initBlock], [b * stepVal, stepBlock]
        // at loop header
        LinkedHashMap<BasicInductionVariable, LinkedHashMap<Integer, IRRegister>> optimized = new LinkedHashMap<>();
        derivedInductionVariable.forEach((reg, div) -> {
            BasicInductionVariable family = div.getFamily();
            // coalesce equivalent div
            if (optimized.containsKey(family) && optimized.get(family).containsKey(div.getB())) {
                div.getParentInstruction().replaceAllUseWithValue(optimized.get(family).get(div.getB()));
                return;
            }
            IRRegister alias = new IRRegister(IRModule.intType, reg.getName() + "_alias");
            IRPhiInstruction newPhi = new IRPhiInstruction(loop.getHeader(), alias, IRModule.intType);
            newPhi.addCandidate(new IRConstInt(div.getB() * family.getInitVal()), family.getInitBlock());
            IRRegister stepVal = new IRRegister(IRModule.intType, "strength_reduction_step_val");
            IRBinaryInstruction calcStep = new IRBinaryInstruction(family.getStepBlock(), "add", stepVal, alias, new IRConstInt(div.getB() * family.getStepVal()));
            family.getStepBlock().insertInstructionBeforeEscape(calcStep);
            newPhi.addCandidate(stepVal, family.getStepBlock());
            loop.getHeader().insertPhiInstruction(newPhi);
            div.getParentInstruction().replaceAllUseWithValue(alias);
            div.getParentInstruction().removeFromParentBlock();
            optimized.putIfAbsent(family, new LinkedHashMap<>());
            optimized.get(family).put(div.getB(), alias);
        });
        changed |= derivedInductionVariable.size() != 0;
    }

    private void doiLoop(IRLoop loop) {
        this.loop = loop;
        inductionVariableAnalysis();
        strengthReduction();
        loop.forEachInnerLoop(this::doiLoop);
    }

    @Override
    public void visit(IRFunction function) {
        new LoopExtractor().extract(function);
        function.getTopLoops().forEach(this::doiLoop);
    }
}

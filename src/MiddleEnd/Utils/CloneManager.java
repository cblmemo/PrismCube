package MiddleEnd.Utils;

import IR.IRBasicBlock;
import IR.Operand.IROperand;

import java.util.LinkedHashMap;

/**
 * This class manage instruction clone in FunctionInliner.
 *
 * @see MiddleEnd.IROptimize.FunctionInliner
 * @author rainy memory
 * @version 1.0.0
 */

public class CloneManager {
    private final LinkedHashMap<IROperand, IROperand> operandClone;
    private final LinkedHashMap<IRBasicBlock, IRBasicBlock> blockClone;

    public CloneManager(LinkedHashMap<IROperand, IROperand> operandClone, LinkedHashMap<IRBasicBlock, IRBasicBlock> blockClone) {
        this.operandClone = operandClone;
        this.blockClone = blockClone;
    }

    public IROperand get(IROperand operand) {
        return operandClone.get(operand);
    }

    public IRBasicBlock get(IRBasicBlock block) {
        return blockClone.get(block);
    }
}

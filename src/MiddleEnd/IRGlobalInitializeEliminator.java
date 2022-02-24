package MiddleEnd;

import IR.IRFunction;
import IR.IRGlobalDefine;
import IR.IRModule;
import IR.Instruction.IRInstruction;
import IR.Instruction.IRStoreInstruction;
import IR.Operand.IRConst;
import IR.Operand.IRGlobalVariableRegister;
import Memory.Memory;
import MiddleEnd.Pass.IRFunctionPass;

import java.util.ArrayList;

public class IRGlobalInitializeEliminator implements IRFunctionPass {
    private IRModule module;

    public void eliminate(Memory memory) {
        this.module = memory.getIRModule();
        ArrayList<IRFunction> initFuncs = new ArrayList<>(module.getSingleInitializeFunctions());
        initFuncs.forEach(this::visit);
        module.tryRemoveGlobalConstructor();
    }

    @Override
    public void visit(IRFunction function) {
        if (function.getBlocks().size() == 2) {
            ArrayList<IRInstruction> instructions = function.getEntryBlock().getInstructions();
            assert instructions.size() >= 2;
            IRInstruction suspicious = instructions.get(instructions.size() - 2);
            if (instructions.size() == 2 && suspicious instanceof IRStoreInstruction && ((IRStoreInstruction) suspicious).getStoreValue() instanceof IRConst) {
                assert ((IRStoreInstruction) suspicious).getStoreAddress() instanceof IRGlobalVariableRegister;
                String variableName = ((IRGlobalVariableRegister) ((IRStoreInstruction) suspicious).getStoreAddress()).getGlobalVariableName();
                IRGlobalDefine define = module.getGlobalDefine(variableName);
                define.setInitValue(((IRConst) ((IRStoreInstruction) suspicious).getStoreValue()).toIROperand());
                module.removeSingleInitializeFunction(function);
            }
        }
    }
}

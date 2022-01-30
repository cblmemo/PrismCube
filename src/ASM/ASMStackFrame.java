package ASM;

// Frame structure:
// --- previous frame     ---
// --- other request      ---
// --- ir alloca register --- ( all alloca instruction will be assigned a unique address in stack )
// --- function arguments --- ( sw arguments if more than 8 is needed when calling other function )

import IR.Instruction.IRAllocaInstruction;
import IR.Operand.IRRegister;

import java.util.HashMap;

import static Debug.MemoLog.log;

public class ASMStackFrame {
    private int maxArgumentNumber = -1;
    private int argumentSize = 0;
    private int allocaSize = 0;
    private int spillSize = 0;
    private final HashMap<IRRegister, Integer> alloca2offset = new HashMap<>();

    public void updateMaxArgumentNumber(int num) {
        maxArgumentNumber = Integer.max(maxArgumentNumber, num);
        argumentSize = maxArgumentNumber > 8 ? 4 * (maxArgumentNumber - 8) : 0;
    }

    private int upperPowerOf2(int num) {
        int ret = 1;
        while (ret < num) ret <<= 1;
        return ret;
    }

    public void requestAlloca(IRAllocaInstruction inst) {
        alloca2offset.put(inst.getAllocaTarget(), argumentSize + allocaSize);
        // at least alloca a word
        int instructionAllocaSize = Math.max(4, upperPowerOf2(inst.getAllocaType().sizeof()));
        log.Debugf("request alloca: size %d at %d\n", instructionAllocaSize, argumentSize + allocaSize);
        allocaSize += instructionAllocaSize;
    }

    public int getAllocaRegisterOffset(IRRegister register) {
        assert alloca2offset.containsKey(register);
        assert alloca2offset.get(register) != null;
        return alloca2offset.get(register);
    }

    public boolean isAllocaRegister(IRRegister register) {
        return alloca2offset.containsKey(register);
    }

    public int spillToStack() {
        spillSize += 4;
        return argumentSize + allocaSize + spillSize - 4;
    }

    public int getFrameSize() {
        return argumentSize + allocaSize + spillSize;
    }
}

package ASM;

// Frame structure:
// --- previous frame     ---
// --- other request      ---
// --- ir alloca register --- ( all alloca instruction will be assigned a unique address in stack )
// --- function arguments --- ( sw arguments if more than 8 is needed when calling other function )

import IR.Instruction.IRAllocaInstruction;
import IR.Operand.IRRegister;

import java.util.HashMap;

public class ASMStackFrame {
    private int requestNumber = 0;
    private int requestSize = 0;
    private int maxArgumentNumber = -1;
    private int argumentSize = 0;
    private int allocaSize = 0;
    private final HashMap<IRRegister, Integer> alloca2offset = new HashMap<>();

    public void updateMaxArgumentNumber(int num) {
        maxArgumentNumber = Integer.max(maxArgumentNumber, num);
        argumentSize = maxArgumentNumber > 8 ? 4 * (maxArgumentNumber - 8) : 0;
    }

    public void requestAlloca(IRAllocaInstruction inst) {
        alloca2offset.put(inst.getAllocaTarget(), argumentSize + allocaSize);
        allocaSize += inst.getAllocaType().sizeof();
    }

    public int getAllocaRegisterOffset(IRRegister register) {
        assert alloca2offset.containsKey(register);
        return alloca2offset.get(register);
    }

    public int requestWord() {
        requestNumber++;
        requestSize += 4;
        return argumentSize + allocaSize + requestSize - 4;
    }

    public int getFrameSize() {
        return argumentSize + allocaSize + requestSize;
    }
}

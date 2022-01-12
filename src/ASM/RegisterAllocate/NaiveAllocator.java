package ASM.RegisterAllocate;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Instruction.ASMArithmeticInstruction;
import ASM.Instruction.ASMInstruction;
import ASM.Instruction.ASMMemoryInstruction;
import ASM.Operand.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NaiveAllocator {
    private static final ArrayList<ASMPhysicalRegister> registers = new ArrayList<>(Arrays.asList(
            ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.t0),
            ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.t1),
            ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.t2)
    ));

    private final ASMFunction function;
    private final HashMap<ASMVirtualRegister, ASMAddress> vr2addr = new HashMap<>();
    private ArrayList<ASMInstruction> newList;

    public NaiveAllocator(ASMFunction function) {
        this.function = function;
    }

    public void allocate() {
        // calculate frame size
        ASMPhysicalRegister sp = ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp);
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst != null) inst.getOperands().forEach(operand -> {
                if (operand instanceof ASMVirtualRegister && !vr2addr.containsKey(((ASMVirtualRegister) operand))) {
                    int offset = function.getStackFrame().requestWord();
                    ASMAddress address = new ASMAddress(sp, new ASMImmediate(offset));
                    vr2addr.put((ASMVirtualRegister) operand, address);
                }
            });
        }));
        // minus & plus sp
        int frameSize = function.getStackFrame().getFrameSize();
        ASMBasicBlock entry = function.getBlocks().get(0), escape = function.getBlocks().get(function.getBlocks().size() - 1);
        int indexOfMinusSp = entry.getInstructions().indexOf(null);
        ASMArithmeticInstruction minusSp = new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.addi);
        minusSp.addOperand(sp).addOperand(sp).addOperand(new ASMImmediate(-frameSize));
        entry.getInstructions().set(indexOfMinusSp, minusSp);
        int indexOfPlusSp = escape.getInstructions().indexOf(null);
        ASMArithmeticInstruction plusSp = new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.addi);
        plusSp.addOperand(sp).addOperand(sp).addOperand(new ASMImmediate(frameSize));
        escape.getInstructions().set(indexOfPlusSp, plusSp);
        // step into block
        function.getBlocks().forEach(this::allocateBlock);
    }

    private void allocateBlock(ASMBasicBlock block) {
        newList = new ArrayList<>();
        block.getInstructions().forEach(this::allocateInstruction);
        block.setInstructions(newList);
    }

    private void allocateInstruction(ASMInstruction inst) {
        if (inst.isStoreInstruction()) {
            ASMOperand rs = inst.getOperands().get(0);
            if (rs instanceof ASMVirtualRegister) {
                newList.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.lw, registers.get(0), vr2addr.get((ASMVirtualRegister) rs)));
                inst.setOperand(0, registers.get(0));
            }
            ASMOperand address = inst.getOperands().get(1);
            assert address instanceof ASMAddress;
            if (((ASMAddress) address).getRegister() instanceof ASMVirtualRegister) {
                ASMAddress tempAddress = vr2addr.get((ASMVirtualRegister) ((ASMAddress) address).getRegister());
                newList.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.lw, registers.get(1), tempAddress));
                ((ASMAddress) address).replaceRegister(registers.get(1));
            }
            newList.add(inst);
        } else {
            for (int i = 1; i < inst.getOperands().size(); i++) {
                ASMOperand rs = inst.getOperands().get(i);
                if (rs instanceof ASMVirtualRegister) {
                    newList.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.lw, registers.get(i), vr2addr.get((ASMVirtualRegister) rs)));
                    inst.setOperand(i, registers.get(i));
                } else if (rs instanceof ASMAddress) { // ir load
                    if (((ASMAddress) rs).getRegister() instanceof ASMVirtualRegister) {
                        ASMAddress tempAddress = vr2addr.get((ASMVirtualRegister) ((ASMAddress) rs).getRegister());
                        newList.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.lw, registers.get(i), tempAddress));
                        ((ASMAddress) rs).replaceRegister(registers.get(i));
                    }
                }
            }
            newList.add(inst);
            if (inst.getOperands().size() != 0) {
                ASMOperand rd = inst.getOperands().get(0);
                if (rd instanceof ASMVirtualRegister) {
                    newList.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.sw, registers.get(0), vr2addr.get((ASMVirtualRegister) rd)));
                    inst.setOperand(0, registers.get(0));
                }
            }
        }
    }
}

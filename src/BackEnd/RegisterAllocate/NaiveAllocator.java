package BackEnd.RegisterAllocate;

import ASM.ASMBasicBlock;
import ASM.ASMFunction;
import ASM.Instruction.ASMArithmeticInstruction;
import ASM.Instruction.ASMInstruction;
import ASM.Instruction.ASMMemoryInstruction;
import ASM.Instruction.ASMPseudoInstruction;
import ASM.Operand.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static Debug.MemoLog.log;

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

    private boolean isValidImmediate(int imm) {
        return -2048 <= imm && imm <= 2047;
    }

    private ASMPhysicalRegister s(int i) {
        return ASMPhysicalRegister.getStoreRegister(i);
    }

    public void allocate() {
        // following code will store some value in s0 - s11, for we don't actually use physical registers except for t0, t1, t2
        // and these save registers are callee save and will be saved by all builtin function we called
        // simultaneously we need to back up these register as well
        ASMPhysicalRegister sp = ASMPhysicalRegister.getPhysicalRegister(ASMPhysicalRegister.PhysicalRegisterName.sp);
        ArrayList<ASMAddress> sBackup = new ArrayList<>();
        sBackup.add(null); // don't need to back up s0
        for (int i = 1; i <= 11; i++) sBackup.add(new ASMAddress(sp, new ASMImmediate(function.getStackFrame().spillToStack())));
        log.Debugf("start allocate register for function %s\n", function.getFunctionName());
        // calculate frame size
        function.getBlocks().forEach(block -> block.getInstructions().forEach(inst -> {
            if (inst != null) inst.getOperands().forEach(operand -> {
                if (operand instanceof ASMVirtualRegister && !vr2addr.containsKey(((ASMVirtualRegister) operand))) {
                    int offset = function.getStackFrame().spillToStack();
                    log.Debugf("request a word at %d" + " ".repeat(6 - Integer.toString(offset).length()) + "for virtual register %s\n", offset, ((ASMVirtualRegister) operand).getName());
                    ASMAddress address;
                    // use si to store sp + 2048 * i
                    if (isValidImmediate(offset)) address = new ASMAddress(sp, new ASMImmediate(offset));
                    else address = new ASMAddress(s(offset / 2048), new ASMImmediate(offset % 2048));
                    vr2addr.put((ASMVirtualRegister) operand, address);
                }
            });
        }));
        // minus & plus sp
        int frameSize = function.getStackFrame().getFrameSize();
        ASMBasicBlock entry = function.getBlocks().get(0), escape = function.getBlocks().get(function.getBlocks().size() - 1);
        int indexOfMinusSp = entry.getInstructions().indexOf(null), indexOfPlusSp = escape.getInstructions().indexOf(null);
        ASMArithmeticInstruction minusSp, plusSp;
        if (isValidImmediate(frameSize)) {
            minusSp = new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.addi);
            minusSp.addOperand(sp).addOperand(sp).addOperand(new ASMImmediate(-frameSize));
            plusSp = new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.addi);
            plusSp.addOperand(sp).addOperand(sp).addOperand(new ASMImmediate(frameSize));
        } else {
            ASMPseudoInstruction liNegative = new ASMPseudoInstruction(ASMPseudoInstruction.InstType.li);
            liNegative.addOperand(s(0)).addOperand(new ASMImmediate(-frameSize));
            minusSp = new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.add);
            minusSp.addOperand(sp).addOperand(sp).addOperand(s(0));
            entry.getInstructions().add(indexOfMinusSp++, liNegative);
            ASMPseudoInstruction liPositive = new ASMPseudoInstruction(ASMPseudoInstruction.InstType.li);
            liPositive.addOperand(s(0)).addOperand(new ASMImmediate(frameSize));
            plusSp = new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.add);
            plusSp.addOperand(sp).addOperand(sp).addOperand(s(0));
            escape.getInstructions().add(indexOfPlusSp++, liPositive);
            // si stores sp + 2048 * i
            int indexOfInitializeStore = indexOfMinusSp + 1;
            int indexOfRetrieveBackup = indexOfPlusSp - 1;
            for (int i = 1; i <= frameSize / 2048; i++) {
                // backup si to stack
                ASMMemoryInstruction backup = new ASMMemoryInstruction(ASMMemoryInstruction.InstType.sw, s(i), sBackup.get(i));
                entry.getInstructions().add(indexOfInitializeStore++, backup);
                ASMMemoryInstruction retrieve = new ASMMemoryInstruction(ASMMemoryInstruction.InstType.lw, s(i), sBackup.get(i));
                escape.getInstructions().add(indexOfRetrieveBackup++, retrieve);
                indexOfPlusSp++;
                // store sp + 2048 * i to si
                ASMPseudoInstruction li2si = new ASMPseudoInstruction(ASMPseudoInstruction.InstType.li);
                li2si.addOperand(ASMPhysicalRegister.getStoreRegister(i)).addOperand(new ASMImmediate(i * 2048));
                entry.getInstructions().add(indexOfInitializeStore++, li2si);
                ASMArithmeticInstruction add2si = new ASMArithmeticInstruction(ASMArithmeticInstruction.InstType.add);
                add2si.addOperand(ASMPhysicalRegister.getStoreRegister(i)).addOperand(sp).addOperand(ASMPhysicalRegister.getStoreRegister(i));
                entry.getInstructions().add(indexOfInitializeStore++, add2si);
            }
        }
        entry.getInstructions().set(indexOfMinusSp, minusSp);
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
            for (int i = inst.isBranchInstruction() ? 0 : 1; i < inst.getOperands().size(); i++) {
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
            if (inst.getOperands().size() != 0 && !inst.isBranchInstruction()) {
                ASMOperand rd = inst.getOperands().get(0);
                if (rd instanceof ASMVirtualRegister) {
                    newList.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.sw, registers.get(0), vr2addr.get((ASMVirtualRegister) rd)));
                    inst.setOperand(0, registers.get(0));
                }
            }
        }


//        if (inst.isCallInstruction()) {
//            newList.add(inst);
//            return;
//        }
//        int current = 0;
//        ArrayList<ASMRegister> use = inst.getUses(), def = inst.getDefs();
//        for (ASMRegister rs : use) {
//            if (rs instanceof ASMVirtualRegister) {
//                ASMPhysicalRegister phyReg = registers.get(current++);
//                newList.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.lw, phyReg, vr2addr.get((ASMVirtualRegister) rs)));
//                inst.replaceRegister((ASMVirtualRegister) rs, phyReg);
//            }
//        }
//        newList.add(inst);
//        for (ASMRegister rd : def) {
//            if (rd instanceof ASMVirtualRegister) {
//                ASMPhysicalRegister phyReg = registers.get(current++);
//                newList.add(new ASMMemoryInstruction(ASMMemoryInstruction.InstType.sw, phyReg, vr2addr.get((ASMVirtualRegister) rd)));
//                inst.replaceRegister((ASMVirtualRegister) rd, phyReg);
//            }
//        }
    }
}

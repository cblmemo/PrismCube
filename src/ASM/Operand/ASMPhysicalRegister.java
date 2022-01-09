package ASM.Operand;

import java.util.ArrayList;
import java.util.HashMap;

public class ASMPhysicalRegister extends ASMRegister {
    public enum PhysicalRegisterName {
        zero, ra, sp, gp, tp,
        t0, t1, t2,
        s0, s1,
        a0, a1, a2, a3, a4, a5, a6, a7,
        s2, s3, s4, s5, s6, s7, s8, s9, s10, s11,
        t3, t4, t5, t6;

        public boolean isTemp() {
            return t0.ordinal() <= ordinal() && ordinal() <= t2.ordinal() || t3.ordinal() <= ordinal() && ordinal() <= t6.ordinal();
        }

        public boolean isArgument() {
            return a0.ordinal() <= ordinal() && ordinal() <= a7.ordinal();
        }

        public boolean isReturnAddress() {
            return ra.ordinal() == ordinal();
        }

        public boolean isSave() {
            return s0.ordinal() <= ordinal() && ordinal() <= s1.ordinal() || s2.ordinal() <= ordinal() && ordinal() <= s11.ordinal();
        }
        
        public boolean isSp() {
            return ordinal() == sp.ordinal();
        }
    }

    static private final HashMap<PhysicalRegisterName, ASMPhysicalRegister> PhysicalRegisters = new HashMap<>();
    static private final ArrayList<ASMPhysicalRegister> CallerSaveRegisters = new ArrayList<>();
    static private final ArrayList<ASMPhysicalRegister> CalleeSaveRegisters = new ArrayList<>();

    static {
        for (PhysicalRegisterName name : PhysicalRegisterName.values()) {
            ASMPhysicalRegister pr = new ASMPhysicalRegister(name);
            PhysicalRegisters.put(name, pr);
            if (name.isTemp() || name.isArgument() || name.isReturnAddress()) CallerSaveRegisters.add(pr);
            if (name.isSave()) CalleeSaveRegisters.add(pr);
        }
    }

    static public ASMPhysicalRegister getPhysicalRegister(PhysicalRegisterName name) {
        return PhysicalRegisters.get(name);
    }

    static public ASMPhysicalRegister getArgumentRegister(int i) {
        return PhysicalRegisters.get(PhysicalRegisterName.values()[PhysicalRegisterName.a0.ordinal() + i]);
    }

    static public ArrayList<ASMPhysicalRegister> getCallerSaveRegisters() {
        return CallerSaveRegisters;
    }

    static public ArrayList<ASMPhysicalRegister> getCalleeSaveRegisters() {
        return CalleeSaveRegisters;
    }

    private final PhysicalRegisterName name;

    public ASMPhysicalRegister(PhysicalRegisterName name) {
        this.name = name;
    }

    public PhysicalRegisterName getName() {
        return name;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}

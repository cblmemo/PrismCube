package IR.TypeSystem;

import BackEnd.InstructionSelector;
import IR.Operand.IRNull;
import IR.Operand.IROperand;
import Memory.Memory;

public class IRPointerType extends IRTypeSystem {
    private final IRTypeSystem baseType;

    public IRPointerType(IRTypeSystem baseType) {
        this.baseType = baseType;
    }

    public static IRTypeSystem constructIRPointerType(IRTypeSystem baseType, int dimension) {
        if (dimension == 0) return baseType;
        return constructIRPointerType(new IRPointerType(baseType), dimension - 1);
    }

    public IRTypeSystem getBaseType() {
        return baseType;
    }

    @Override
    public String toString() {
        return baseType.toString() + "*";
    }

    @Override
    public IROperand getDefaultValue() {
        return new IRNull(this);
    }

    @Override
    public int sizeof() {
        // ravel: 32-bit, my computer: 64-bit
        return Memory.getArchitecture() == Memory.Architecture.x86_32 ? 4 : 8;
    }
}

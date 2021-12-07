package IR.Operand;

import IR.TypeSystem.IRTypeSystem;

public class IRZeroInitializer extends IROperand {
    public IRZeroInitializer(IRTypeSystem irType) {
        super(irType);
    }

    @Override
    public String toString() {
        return "zeroinitializer";
    }
}

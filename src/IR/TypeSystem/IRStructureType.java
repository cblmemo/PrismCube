package IR.TypeSystem;

import FrontEnd.IRVisitor;
import IR.Operand.IRConstNumber;
import IR.Operand.IROperand;
import IR.Operand.IRZeroInitializer;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IRStructureType extends IRTypeSystem {
    private final String className;
    private final LinkedHashMap<String, IRTypeSystem> memberTypes = new LinkedHashMap<>();
    private final LinkedHashMap<String, Integer> memberIndices = new LinkedHashMap<>();
    private int memberNumber = 0;
    private boolean hasCustomConstructor = false;

    public IRStructureType(String className) {
        this.className = className;
    }

    public int addMember(String memberName, IRTypeSystem memberType) {
        memberTypes.put(memberName, memberType);
        memberIndices.put(memberName, memberNumber);
        return memberNumber++;
    }

    public String getClassDeclare() {
        StringBuilder builder = new StringBuilder();
        builder.append("type { ");
        var iter = memberTypes.entrySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            var entry = iter.next();
            if ((i++) != 0) builder.append(", ");
            builder.append(entry.getValue().toString());
        }
        builder.append(" }");
        return "%class." + className + " = " + builder;
    }

    public String getClassName() {
        return className;
    }

    public int getMemberIndex(String memberName) {
        assert memberIndices.containsKey(memberName);
        return memberIndices.get(memberName);
    }

    public int getMemberOffset(int index) {
        AtomicInteger ret = new AtomicInteger();
        memberTypes.forEach((name, type) -> {
            if (memberIndices.get(name) < index) ret.addAndGet(type.sizeof());
        });
        return ret.get();
    }

    public boolean hasCustomConstructor() {
        return hasCustomConstructor;
    }

    public void setAsHasCustomConstructor() {
        hasCustomConstructor = true;
    }

    @Override
    public String toString() {
        return "%class." + className;
    }

    @Override
    public IROperand getDefaultValue() {
        return new IRZeroInitializer(this);
    }

    @Override
    public IRConstNumber getCorrespondingConstOperandType() {
        return null;
    }

    @Override
    public int sizeof() {
        AtomicInteger size = new AtomicInteger();
        memberTypes.forEach((name, type) -> size.addAndGet(type.sizeof()));
        int realSize = size.get();
        int ret = 1;
        while (ret < realSize) ret *= 2;
        return ret;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

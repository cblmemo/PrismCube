package IR.Operand;

import FrontEnd.IRVisitor;
import IR.TypeSystem.IRTypeSystem;

public class IRConstString extends IRConst {
    private final String value;
    private final String originalValue;
    private final int id;
    private final int length;

    public IRConstString(IRTypeSystem irType, String value, int id) {
        super(irType);
        String converted = convert(value);
        this.length = converted.length();
        this.value = convertToPlain(converted);
        this.id = id;
        this.originalValue = value;
    }

    public String getValue() {
        return value;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public int getId() {
        return id;
    }

    public int getLength() {
        return length;
    }

    private String getConstStringIdentifier() {
        return "@.str." + id;
    }

    private int getTrueLength() {
        // \0 at the end of string
        return length + 1;
    }

    private String convertToPlain(String src) {
        // avoid hard wrap in *.ll
        return src.replace("\\", "\\5C")
                .replace("\n", "\\0A")
                .replace("\t", "\\09")
                .replace("\"", "\\22");
    }

    private String convert(String src) {
        return src.replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"");
    }

    public String toInitValueStr() {
        return getConstStringIdentifier() + " = private unnamed_addr constant [" + getTrueLength() + " x i8] c\"" + value + "\\00\"";
    }

    @Override
    public String toString() {
        return "getelementptr inbounds ([" + getTrueLength() + " x i8], [" + getTrueLength() + " x i8]* " + getConstStringIdentifier() + ", i32 0, i32 0)";
    }

    @Override
    public IROperand toIROperand() {
        return this;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

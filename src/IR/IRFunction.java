package IR;

import IR.TypeSystem.IRTypeSystem;
import Utility.Type.ArrayType;
import Utility.Type.ClassType;
import Utility.Type.Type;
import Utility.error.IRError;

import java.util.ArrayList;
import java.util.Objects;

import static Debug.MemoLog.log;

public class IRFunction {
    private String functionName;
    private String abbreviatedParameters = "";
    private final boolean declare;
    private IRTypeSystem returnType;
    private final ArrayList<IRTypeSystem> parameterType = new ArrayList<>();
    private final ArrayList<IRBasicBlock> blocks = new ArrayList<>();
    private final IRBasicBlock entryBlock;
    private final IRBasicBlock returnBlock;

    // [[--NOTICE--]] need to call setReturnType and addParameterType manually after created an IRFunction instance.
    public IRFunction(String functionName) {
        this.functionName = functionName;
        this.declare = false;
        entryBlock = new IRBasicBlock(this, "entry");
        blocks.add(entryBlock);
        returnBlock = new IRBasicBlock(this, "return");
        returnBlock.markAsReturnBlock();
    }

    public IRFunction(String functionName, boolean declare) {
        this.functionName = functionName;
        this.declare = declare;
        entryBlock = new IRBasicBlock(this, "entry");
        blocks.add(entryBlock);
        returnBlock = new IRBasicBlock(this, "return");
        returnBlock.markAsReturnBlock();
    }

    public void finishFunction() {
        blocks.add(returnBlock);
    }

    //   \/ --- after a day and a half I discover it is useless since Mx* doesn't support override :( --- \/

    private String mangleIdentifiers(String identifier) {
        // mangle identifier with length + identifier
        // e.g. f -> 1f
        // to avoid abbreviation collide like
        // <void i(int a, int b) {}> and <void ii(int b) {}>
        // otherwise two function will both mangle to <iii>
        // with identifier length, <void i(int a, int b) {}> -> <1iii>
        // and <void ii(int b) {}> -> <2iii>
        // since identifier cannot start with numbers, we don't need
        // to add delim between length and identifier.
        return identifier.length() + identifier;
    }

    private String mangleParameterTypes(Type type) {
        if (type.isInt()) return "i";
        if (type.isBool()) return "b";
        if (type.isString()) return "s";
        if (type instanceof ArrayType)
            return "P".repeat(Math.max(0, ((ArrayType) type).getDimension())) +
                    mangleParameterTypes(((ArrayType) type).getRootElementType());
        if (type instanceof ClassType)
            return mangleIdentifiers(type.getTypeName());
        throw new IRError("cannot mangle parameter types: " + type.getTypeName());
    }

    public void mangle() {
        // cannot mangle main function and builtin function
        if (Objects.equals(functionName, "main")) return;
        functionName = "_Z" + mangleIdentifiers(functionName) + // no arguments refer to void
                (Objects.equals(abbreviatedParameters, "") ? "v" : abbreviatedParameters);
    }

    public void printName() {
        // only for debug use
        log.Debugf(functionName);
    }

    //   /\ --- after a day and a half I discover it is useless since Mx* doesn't support override :( --- /\

    public boolean isDeclare() {
        return declare;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setReturnType(IRTypeSystem returnType) {
        this.returnType = returnType;
    }

    public IRTypeSystem getReturnType() {
        return returnType;
    }

    public void addParameterType(Type paraType, IRTypeSystem parameterType) {
        abbreviatedParameters += mangleParameterTypes(paraType);
        this.parameterType.add(parameterType);
    }

    public ArrayList<IRTypeSystem> getParameterType() {
        return parameterType;
    }

    public IRBasicBlock getEntryBlock() {
        return entryBlock;
    }

    public IRBasicBlock getReturnBlock() {
        return returnBlock;
    }

    public ArrayList<IRBasicBlock> getBlocks() {
        return blocks;
    }

    private String getParameterListStr() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (int i = 0; i < getParameterType().size(); i++) {
            if (i != 0) builder.append(", ");
            builder.append(parameterType.get(i).toString());
        }
        builder.append(")");
        return builder.toString();
    }

    public String getDeclare() {
        return "declare " + returnType.toString() + " @" + functionName + getParameterListStr() + " #0";
    }

    public String getDefineAndPrefix() {
        return "define " + returnType.toString() + " @" + functionName + getParameterListStr() + (" #0 {");
    }

    public String getSuffix() {
        return "}";
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

package IR;

import FrontEnd.IRVisitor;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRTypeSystem;
import Utility.Type.ArrayType;
import Utility.Type.ClassType;
import Utility.Type.Type;
import Utility.error.IRError;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;

import static Debug.MemoLog.log;

public class IRFunction {
    private String functionName;
    private String abbreviatedParameters = "";
    private final boolean declare;
    private IRTypeSystem returnType;
    private final ArrayList<IRRegister> parameters = new ArrayList<>();
    private final ArrayList<IRTypeSystem> parameterType = new ArrayList<>();
    private final ArrayList<String> parameterName = new ArrayList<>();
    private final ArrayList<IRBasicBlock> blocks = new ArrayList<>();
    private final IRBasicBlock entryBlock;
    private final IRBasicBlock returnBlock;
    private boolean hasCalled;
    private IRRegister thisRegister;

    // [[--NOTICE--]] need to call setReturnType and addParameterType manually after created an IRFunction instance.
    public IRFunction(String functionName) {
        this.functionName = functionName;
        this.declare = false;
        entryBlock = new IRBasicBlock(this, "entry");
        entryBlock.markAsEntryBlock();
        returnBlock = new IRBasicBlock(this, "return");
        returnBlock.markAsReturnBlock();
    }

    public IRFunction(String functionName, boolean declare) {
        this.functionName = functionName;
        this.declare = declare;
        entryBlock = new IRBasicBlock(this, "entry");
        entryBlock.markAsEntryBlock();
        returnBlock = new IRBasicBlock(this, "return");
        returnBlock.markAsReturnBlock();
    }

    public void appendBasicBlock(IRBasicBlock block) {
        blocks.add(block);
    }

    public void finishFunction() {
        returnBlock.finishBlock();
        blocks.add(returnBlock);
    }

//    public void changeLabelToNumber() {
//        HashMap<String, Integer> label2num = new HashMap<>();
//        int current = 0, offset = 0;
//        for (int i = 0; i < blocks.size(); i++) {
//            IRBasicBlock currentBlock = blocks.get(i);
//            label2num.put(currentBlock.getLabel().getLabelName(), current);
//            currentBlock.getLabel().setRegisterNum(current++);
//
//        }
//
//    }

    //   \/ --- after a day and a half I discover it is useless since Mx* doesn't support override :(

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

    //   /\ --- after a day and a half I discover it is useless since Mx* doesn't support override :(

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

    public void addParameter(String parameterName, IRTypeSystem parameterType) {
//        abbreviatedParameters += mangleParameterTypes(paraType);
        this.parameters.add(new IRRegister(parameterType, "argument"));
        this.parameterName.add(parameterName);
        this.parameterType.add(parameterType);
    }

    public void builtinAddParameter(IRTypeSystem parameterType) {
//        abbreviatedParameters += mangleParameterTypes(paraType);
        this.parameters.add(null);
        this.parameterName.add(""); // builtin function won't use this
        this.parameterType.add(parameterType);
    }

    public ArrayList<IRRegister> getParameters() {
        return parameters;
    }

    public ArrayList<IRTypeSystem> getParameterType() {
        return parameterType;
    }

    public ArrayList<String> getParameterName() {
        return parameterName;
    }

    public int getParameterNumber() {
        return parameters.size();
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
        for (int i = 0; i < parameters.size(); i++) {
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

    public boolean hasCalled() {
        return hasCalled;
    }

    public void markAsCalled() {
        hasCalled = true;
    }

    public void setThisRegister(IRRegister thisRegister) {
        this.thisRegister = thisRegister;
    }

    public IRRegister getThisRegister() {
        return thisRegister;
    }

    private final ArrayList<IRBasicBlock> reachable = new ArrayList<>();

    public ArrayList<IRBasicBlock> reachableBlocks() {
        reachable.clear();
        dfs(entryBlock);
        return reachable;
    }

    private void dfs(IRBasicBlock current) {
        reachable.add(current);
        current.getSuccessors().forEach(succ -> {
            if (!reachable.contains(succ)) dfs(succ);
        });
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

package IR;

import FrontEnd.IRVisitor;
import IR.Instruction.IRInstruction;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRTypeSystem;
import MiddleEnd.Utils.IRLoop;
import Utility.Type.ArrayType;
import Utility.Type.ClassType;
import Utility.Type.Type;
import Utility.error.IRError;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private ArrayList<IRBasicBlock> blocks = new ArrayList<>();
    private final IRBasicBlock entryBlock;
    private IRBasicBlock returnBlock;
    private boolean hasCalled;
    private IRRegister thisRegister;
    private LinkedHashMap<IRBasicBlock, LinkedHashSet<IRBasicBlock>> dominatorFrontier;
    private LinkedHashMap<IRBasicBlock, LinkedHashSet<IRBasicBlock>> postDominatorFrontier;
    private int forceInlineCnt = 0;
    private final LinkedHashSet<IRLoop> topLoops = new LinkedHashSet<>();

    // [[--NOTICE--]] need to call setReturnType and addParameterType manually after created an IRFunction instance.
    public IRFunction(String functionName) {
        this.functionName = functionName;
        this.declare = false;
        entryBlock = new IRBasicBlock(this, "entry");
        entryBlock.initializeAllocas();
        returnBlock = new IRBasicBlock(this, "return");
        returnBlock.markReturnBlock(true);
    }

    public IRFunction(String functionName, boolean declare) {
        this.functionName = functionName;
        this.declare = declare;
        entryBlock = new IRBasicBlock(this, "entry");
        entryBlock.initializeAllocas();
        returnBlock = new IRBasicBlock(this, "return");
        returnBlock.markReturnBlock(true);
    }

    public void appendBasicBlock(IRBasicBlock block) {
        blocks.add(block);
    }

    public void finishFunction() {
        returnBlock.finishBlock();
        blocks.add(returnBlock);
    }

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

    public void setBlocks(ArrayList<IRBasicBlock> blocks) {
        this.blocks = blocks;
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

    public boolean removeUnreachableBlocks() {
        reachableBlocks();
        boolean ret = reachable.size() != blocks.size();
        ArrayList<IRBasicBlock> blockBackup = new ArrayList<>(blocks);
        blockBackup.forEach(block -> {
            if (!reachable.contains(block)) {
                assert block != entryBlock && block != returnBlock : block;
                blocks.remove(block);
                block.getPredecessors().forEach(pred -> pred.removeSuccessor(block));
                block.getSuccessors().forEach(succ -> succ.removePredecessor(block));
            }
        });
        return ret;
    }

    public void setDominatorFrontier(LinkedHashMap<IRBasicBlock, LinkedHashSet<IRBasicBlock>> dominatorFrontier) {
        this.dominatorFrontier = dominatorFrontier;
    }

    public LinkedHashMap<IRBasicBlock, LinkedHashSet<IRBasicBlock>> getDominatorFrontier() {
        return dominatorFrontier;
    }

    public void setPostDominatorFrontier(LinkedHashMap<IRBasicBlock, LinkedHashSet<IRBasicBlock>> postDominatorFrontier) {
        this.postDominatorFrontier = postDominatorFrontier;
    }

    public LinkedHashMap<IRBasicBlock, LinkedHashSet<IRBasicBlock>> getPostDominatorFrontier() {
        return postDominatorFrontier;
    }

    public void relocatePhis() {
        blocks.forEach(block -> {
            ArrayList<IRInstruction> instructions = new ArrayList<>(block.getInstructions());
            block.getInstructions().clear();
            block.getInstructions().addAll(block.getPhis());
            block.getInstructions().addAll(instructions);
        });
    }

    public void addAllNewBlocks(LinkedHashSet<IRBasicBlock> newBlocks) {
        assert blocks.get(blocks.size() - 1) == returnBlock;
        blocks.remove(returnBlock);
        blocks.addAll(newBlocks);
        blocks.add(returnBlock);
    }

    public void relocateReturnBlock(IRBasicBlock newReturnBlock) {
        returnBlock = newReturnBlock;
        blocks.remove(returnBlock);
        blocks.add(returnBlock);
    }

    public void setReturnBlock(IRBasicBlock returnBlock) {
        this.returnBlock = returnBlock;
    }

    public int getForceInlineCnt() {
        return forceInlineCnt;
    }

    public void incrementForceInlineCnt() {
        forceInlineCnt++;
    }

    public void addTopLoop(IRLoop loop) {
        topLoops.add(loop);
    }

    public LinkedHashSet<IRLoop> getTopLoops() {
        return topLoops;
    }

    @Override
    public String toString() {
        return functionName;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

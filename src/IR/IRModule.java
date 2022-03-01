package IR;

import FrontEnd.IRVisitor;
import IR.Instruction.IRCallInstruction;
import IR.Instruction.IRInstruction;
import IR.Operand.IRConstString;
import IR.TypeSystem.*;
import Utility.Scope.GlobalScope;
import Utility.error.IRError;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.Consumer;

import static Debug.MemoLog.log;

// Hierarchy of IR:
// -- IRModule
//    -- IRGlobalDefine
//    -- IRFunction
//       -- IRBasicBlock
//          -- IRInstruction

public class IRModule {
    private final LinkedHashMap<String, IRTypeSystem> types = new LinkedHashMap<>();
    private final LinkedHashMap<String, IRFunction> builtinFunctions = new LinkedHashMap<>();
    private final ArrayList<IRStructureType> classes = new ArrayList<>();
    private final LinkedHashMap<String, IRFunction> functions = new LinkedHashMap<>();
    private final LinkedHashMap<String, IRGlobalDefine> globalDefines = new LinkedHashMap<>();
    private final LinkedHashMap<String, IRConstString> strings = new LinkedHashMap<>();
    private int stringCnt = 0;
    private IRFunction mainFunction;
    private final IRFunction globalConstructor;
    private final ArrayList<IRFunction> singleInitializeFunctions = new ArrayList<>();
    private IRCallInstruction callGlobalInit;

    private boolean generatedInitializeFunction = false;
    static private final String globalInitializeFunctionName = "__mx_global_init";

    public static IRTypeSystem nullType;
    public static IRTypeSystem voidType;
    public static IRTypeSystem boolType;
    public static IRTypeSystem charType;
    public static IRTypeSystem intType;
    public static IRTypeSystem stringType;

    private static final String llvmDetails = """
            ; ModuleID = 'src.mx'
            source_filename = "src.mx"
            target datalayout = "e-m:o-i64:64-i128:128-n32:64-S128"
            target triple = "riscv32"
            """;

    public IRModule() {
        addIRType("null", nullType = new IRNullType());
        addIRType("void", voidType = new IRVoidType());
        addIRType("bool", boolType = new IRIntType(1));
        addIRType("char", charType = new IRIntType(8));
        addIRType("int", intType = new IRIntType(32));
        addIRType("string", stringType = new IRPointerType(IRModule.charType));
        addIRType("void *", new IRPointerType(IRModule.voidType));

        globalConstructor = generateGlobalInitializeFunction();
        globalConstructor.setReturnType(IRModule.voidType);
        globalConstructor.appendBasicBlock(globalConstructor.getEntryBlock());
    }

    public void initializeBuiltinFunction(GlobalScope globalScope) {
        IRFunction print = new IRFunction("print", true);
        print.setReturnType(IRModule.voidType);
        print.builtinAddParameter(IRModule.stringType);
        addBuiltinFunction(print);
        globalScope.getFunction("print").setIRFunction(print);

        IRFunction printInt = new IRFunction("printInt", true);
        printInt.setReturnType(IRModule.voidType);
        printInt.builtinAddParameter(IRModule.intType);
        addBuiltinFunction(printInt);
        globalScope.getFunction("printInt").setIRFunction(printInt);

        IRFunction println = new IRFunction("println", true);
        println.setReturnType(IRModule.voidType);
        println.builtinAddParameter(IRModule.stringType);
        addBuiltinFunction(println);
        globalScope.getFunction("println").setIRFunction(println);

        IRFunction printlnInt = new IRFunction("printlnInt", true);
        printlnInt.setReturnType(IRModule.voidType);
        printlnInt.builtinAddParameter(IRModule.intType);
        addBuiltinFunction(printlnInt);
        globalScope.getFunction("printlnInt").setIRFunction(printlnInt);

        IRFunction getString = new IRFunction("getString", true);
        getString.setReturnType(IRModule.stringType);
        addBuiltinFunction(getString);
        globalScope.getFunction("getString").setIRFunction(getString);

        IRFunction getInt = new IRFunction("getInt", true);
        getInt.setReturnType(IRModule.intType);
        addBuiltinFunction(getInt);
        globalScope.getFunction("getInt").setIRFunction(getInt);

        IRFunction toString = new IRFunction("toString", true);
        toString.setReturnType(IRModule.stringType);
        toString.builtinAddParameter(IRModule.intType);
        addBuiltinFunction(toString);
        globalScope.getFunction("toString").setIRFunction(toString);

        IRFunction concatenateString = new IRFunction("__mx_concatenateString", true);
        concatenateString.setReturnType(IRModule.stringType);
        concatenateString.builtinAddParameter(IRModule.stringType);
        concatenateString.builtinAddParameter(IRModule.stringType);
        addBuiltinFunction(concatenateString);

        IRFunction stringLt = new IRFunction("__mx_stringLt", true);
        stringLt.setReturnType(IRModule.charType);
        stringLt.builtinAddParameter(IRModule.stringType);
        stringLt.builtinAddParameter(IRModule.stringType);
        addBuiltinFunction(stringLt);

        IRFunction stringLe = new IRFunction("__mx_stringLe", true);
        stringLe.setReturnType(IRModule.charType);
        stringLe.builtinAddParameter(IRModule.stringType);
        stringLe.builtinAddParameter(IRModule.stringType);
        addBuiltinFunction(stringLe);

        IRFunction stringGt = new IRFunction("__mx_stringGt", true);
        stringGt.setReturnType(IRModule.charType);
        stringGt.builtinAddParameter(IRModule.stringType);
        stringGt.builtinAddParameter(IRModule.stringType);
        addBuiltinFunction(stringGt);

        IRFunction stringGe = new IRFunction("__mx_stringGe", true);
        stringGe.setReturnType(IRModule.charType);
        stringGe.builtinAddParameter(IRModule.stringType);
        stringGe.builtinAddParameter(IRModule.stringType);
        addBuiltinFunction(stringGe);

        IRFunction stringEq = new IRFunction("__mx_stringEq", true);
        stringEq.setReturnType(IRModule.charType);
        stringEq.builtinAddParameter(IRModule.stringType);
        stringEq.builtinAddParameter(IRModule.stringType);
        addBuiltinFunction(stringEq);

        IRFunction stringNe = new IRFunction("__mx_stringNe", true);
        stringNe.setReturnType(IRModule.charType);
        stringNe.builtinAddParameter(IRModule.stringType);
        stringNe.builtinAddParameter(IRModule.stringType);
        addBuiltinFunction(stringNe);

        IRFunction stringLength = new IRFunction("__mx_stringLength", true);
        stringLength.setReturnType(IRModule.intType);
        stringLength.builtinAddParameter(IRModule.stringType);
        addBuiltinFunction(stringLength);

        IRFunction stringSubstring = new IRFunction("__mx_stringSubstring", true);
        stringSubstring.setReturnType(IRModule.stringType);
        stringSubstring.builtinAddParameter(IRModule.stringType);
        stringSubstring.builtinAddParameter(IRModule.intType);
        stringSubstring.builtinAddParameter(IRModule.intType);
        addBuiltinFunction(stringSubstring);

        IRFunction stringParseInt = new IRFunction("__mx_stringParseInt", true);
        stringParseInt.setReturnType(IRModule.intType);
        stringParseInt.builtinAddParameter(IRModule.stringType);
        addBuiltinFunction(stringParseInt);

        IRFunction stringOrd = new IRFunction("__mx_stringOrd", true);
        stringOrd.setReturnType(IRModule.intType);
        stringOrd.builtinAddParameter(IRModule.stringType);
        stringOrd.builtinAddParameter(IRModule.intType);
        addBuiltinFunction(stringOrd);

        IRFunction malloc = new IRFunction("__mx_malloc", true);
        malloc.setReturnType(IRModule.stringType);
        malloc.builtinAddParameter(IRModule.intType);
        addBuiltinFunction(malloc);
    }

    public static String getLLVMDetails() {
        return llvmDetails;
    }

    public IRFunction getGlobalConstructor() {
        return globalConstructor;
    }

//    public String getLLVMGlobalConstructors() {
//        return "@llvm.global_ctors = appending global [1 x { i32, void ()*, i8* }] [{ i32, void ()*, i8* } { i32 65535, void ()* @" + globalConstructor.getFunctionName() + ", i8* null }]";
//    }

    static public String getGlobalInitializeFunctionName() {
        return globalInitializeFunctionName;
    }

    public IRFunction generateSingleInitializeFunction() {
        String initFuncName = globalInitializeFunctionName + "_" + singleInitializeFunctions.size();
        IRFunction initFunc = new IRFunction(initFuncName);
        initFunc.setReturnType(IRModule.voidType);
        singleInitializeFunctions.add(initFunc);
        return initFunc;
    }

    public IRFunction generateGlobalInitializeFunction() {
        if (generatedInitializeFunction) throw new IRError("multi-generated global init");
        generatedInitializeFunction = true;
        return new IRFunction(getGlobalInitializeFunctionName(), false);
    }

    private void addBuiltinFunction(IRFunction function) {
        if (builtinFunctions.containsKey(function.getFunctionName()))
            throw new IRError("duplicated IR builtin function name");
        builtinFunctions.put(function.getFunctionName(), function);
    }

    public IRFunction getBuiltinFunction(String name) {
        assert builtinFunctions.containsKey(name);
        return builtinFunctions.get(name);
    }

    public void addFunction(IRFunction function) {
        if (functions.containsKey(function.getFunctionName())) throw new IRError("duplicated IR function name");
        functions.put(function.getFunctionName(), function);
        if (Objects.equals(function.getFunctionName(), "main")) mainFunction = function;
    }

    public IRFunction getFunction(String name) {
        if (functions.containsKey(name)) return functions.get(name);
        if (builtinFunctions.containsKey(name)) return builtinFunctions.get(name);
        throw new IRError("IR function not found");
    }

    private void addIRType(String name, IRTypeSystem type) {
        if (types.containsKey(name)) throw new IRError("duplicated IR type name");
        types.put(name, type);
    }

    public void addIRClassType(String name, IRStructureType type) {
        addIRType(name, type);
        classes.add(type);
    }

    public IRTypeSystem getIRType(String name) {
        if (types.containsKey(name)) return types.get(name);
        throw new IRError("IR type not found");
    }

    public void addGlobalDefine(IRGlobalDefine define) {
        globalDefines.put(define.getVariableName(), define);
    }

    public IRGlobalDefine getGlobalDefine(String name) {
        assert globalDefines.containsKey(name);
        return globalDefines.get(name);
    }

    public void addNewConstString(String value) {
        if (strings.containsKey(value)) return;
        log.Tracef("receive new string constant \"%s\".\n", value);
        strings.put(value, new IRConstString(value, stringCnt++));
    }

    public int getConstStringId(String tar) {
        if (strings.containsKey(tar)) return strings.get(tar).getId();
        throw new IRError("get string id failed.");
    }

    public ArrayList<IRStructureType> getClasses() {
        return classes;
    }

    public LinkedHashMap<String, IRGlobalDefine> getGlobalDefines() {
        return globalDefines;
    }

    public LinkedHashMap<String, IRConstString> getStrings() {
        return strings;
    }

    public LinkedHashMap<String, IRFunction> getFunctions() {
        return functions;
    }

    public LinkedHashMap<String, IRFunction> getBuiltinFunctions() {
        return builtinFunctions;
    }

    public ArrayList<IRFunction> getSingleInitializeFunctions() {
        return singleInitializeFunctions;
    }

    public void relocateInitializeFunctionsAndAllocas() {
        addFunction(globalConstructor);
        singleInitializeFunctions.forEach(this::addFunction);
        callGlobalInit = new IRCallInstruction(mainFunction.getEntryBlock(), IRModule.voidType, globalConstructor);
        mainFunction.getEntryBlock().getInstructions().add(0, callGlobalInit);
        functions.values().forEach(func -> {
            if (!func.isDeclare()) {
                IRBasicBlock entry = func.getEntryBlock();
                ArrayList<IRInstruction> insts = new ArrayList<>(entry.getInstructions());
                entry.getInstructions().clear();
                entry.getInstructions().addAll(entry.getAllocas());
                entry.getInstructions().addAll(insts);
            }
        });
    }

    public void removeSingleInitializeFunction(IRFunction init) {
        functions.remove(init.getFunctionName());
        singleInitializeFunctions.remove(init);
        IRInstruction target = null;
        for (IRInstruction inst : globalConstructor.getEntryBlock().getInstructions()) {
            if (inst instanceof IRCallInstruction && ((IRCallInstruction) inst).getCallFunction() == init) {
                target = inst;
                break;
            }
        }
        assert target != null;
        globalConstructor.getEntryBlock().getInstructions().remove(target);
    }

    public void tryRemoveGlobalConstructor() {
        if (globalConstructor.getEntryBlock().getInstructions().size() == 1) {
            functions.remove(globalConstructor.getFunctionName());
            int index = mainFunction.getEntryBlock().getInstructions().indexOf(callGlobalInit);
            // might already delete by AggressiveDeadCodeEliminator
            if (index >= 0) {
                log.Debugf("remove inst %s when tryRemoveGlobalConstructor\n", mainFunction.getEntryBlock().getInstructions().get(index));
                mainFunction.getEntryBlock().getInstructions().remove(index);
            }
        }
    }

    public void forEachInstruction(Consumer<IRInstruction> consumer) {
        functions.values().forEach(function -> function.getBlocks().forEach(block -> block.getInstructions().forEach(consumer)));
    }

    public void removeUnusedFunction() {
        LinkedHashSet<IRFunction> called = new LinkedHashSet<>();
        called.add(mainFunction);
        forEachInstruction(inst -> {
            if (inst instanceof IRCallInstruction) called.add(((IRCallInstruction) inst).getCallFunction());
        });
        LinkedHashSet<IRFunction> all = new LinkedHashSet<>(functions.values());
        all.forEach(function -> {
            if (!called.contains(function)) {
                functions.remove(function.getFunctionName());
                singleInitializeFunctions.remove(function);
            }
        });
    }

    public IRFunction getMainFunction() {
        return mainFunction;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

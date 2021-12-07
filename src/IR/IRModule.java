package IR;

import IR.Operand.IRConstString;
import IR.TypeSystem.*;
import Utility.Scope.GlobalScope;
import Utility.error.IRError;

import java.util.ArrayList;
import java.util.HashMap;

import static Debug.MemoLog.log;

// Hierarchy of IR:
// -- IRModule
//    -- IRGlobalDefine
//    -- IRFunction
//       -- IRBasicBlock
//          -- IRInstruction

public class IRModule {
    private final HashMap<String, IRTypeSystem> types = new HashMap<>();
    private final HashMap<String, IRFunction> builtinFunctions = new HashMap<>();
    private final ArrayList<IRStructureType> classes = new ArrayList<>();
    private final HashMap<String, IRFunction> functions = new HashMap<>();
    private final HashMap<String, IRGlobalDefine> globalDefines = new HashMap<>();
    private final HashMap<String, IRConstString> strings = new HashMap<>();
    private int stringCnt = 0;
    private final IRFunction globalConstructor;
    private final ArrayList<IRFunction> singleInitializeFunctions = new ArrayList<>();

    private boolean generatedInitializeFunction = false;
    private final String globalInitializeFunctionName = "__mx_global_init";

    public IRModule() {
        addIRType("null", new IRNullType());
        addIRType("void", new IRVoidType());
        addIRType("bool", new IRIntType(1));
        addIRType("char", new IRIntType(8));
        addIRType("int", new IRIntType(32));
        addIRType("string", new IRPointerType(getIRType("char")));
        addIRType("void *", new IRPointerType(getIRType("void")));

        globalConstructor = generateGlobalInitializeFunction();
        globalConstructor.setReturnType(getIRType("void"));
        globalConstructor.appendBasicBlock(globalConstructor.getEntryBlock());
    }

    public void initializeBuiltinFunction(GlobalScope globalScope) {
        IRFunction print = new IRFunction("print", true);
        print.setReturnType(getIRType("void"));
        print.builtinAddParameter(getIRType("string"));
        addBuiltinFunction(print);
        globalScope.getFunction("print").setIRFunction(print);

        IRFunction printInt = new IRFunction("printInt", true);
        printInt.setReturnType(getIRType("void"));
        printInt.builtinAddParameter(getIRType("int"));
        addBuiltinFunction(printInt);
        globalScope.getFunction("printInt").setIRFunction(printInt);

        IRFunction println = new IRFunction("println", true);
        println.setReturnType(getIRType("void"));
        println.builtinAddParameter(getIRType("string"));
        addBuiltinFunction(println);
        globalScope.getFunction("println").setIRFunction(println);

        IRFunction printlnInt = new IRFunction("printlnInt", true);
        printlnInt.setReturnType(getIRType("void"));
        printlnInt.builtinAddParameter(getIRType("int"));
        addBuiltinFunction(printlnInt);
        globalScope.getFunction("printlnInt").setIRFunction(printlnInt);

        IRFunction getString = new IRFunction("getString", true);
        getString.setReturnType(getIRType("string"));
        addBuiltinFunction(getString);
        globalScope.getFunction("getString").setIRFunction(getString);

        IRFunction getInt = new IRFunction("getInt", true);
        getInt.setReturnType(getIRType("int"));
        addBuiltinFunction(getInt);
        globalScope.getFunction("getInt").setIRFunction(getInt);

        IRFunction toString = new IRFunction("toString", true);
        toString.setReturnType(getIRType("string"));
        toString.builtinAddParameter(getIRType("int"));
        addBuiltinFunction(toString);
        globalScope.getFunction("toString").setIRFunction(toString);

        IRFunction concatenateString = new IRFunction("__mx_concatenateString", true);
        concatenateString.setReturnType(getIRType("string"));
        concatenateString.builtinAddParameter(getIRType("string"));
        concatenateString.builtinAddParameter(getIRType("string"));
        addBuiltinFunction(concatenateString);

        IRFunction stringLt = new IRFunction("__mx_stringLt", true);
        stringLt.setReturnType(getIRType("char"));
        stringLt.builtinAddParameter(getIRType("string"));
        stringLt.builtinAddParameter(getIRType("string"));
        addBuiltinFunction(stringLt);

        IRFunction stringLe = new IRFunction("__mx_stringLe", true);
        stringLe.setReturnType(getIRType("char"));
        stringLe.builtinAddParameter(getIRType("string"));
        stringLe.builtinAddParameter(getIRType("string"));
        addBuiltinFunction(stringLe);

        IRFunction stringGt = new IRFunction("__mx_stringGt", true);
        stringGt.setReturnType(getIRType("char"));
        stringGt.builtinAddParameter(getIRType("string"));
        stringGt.builtinAddParameter(getIRType("string"));
        addBuiltinFunction(stringGt);

        IRFunction stringGe = new IRFunction("__mx_stringGe", true);
        stringGe.setReturnType(getIRType("char"));
        stringGe.builtinAddParameter(getIRType("string"));
        stringGe.builtinAddParameter(getIRType("string"));
        addBuiltinFunction(stringGe);

        IRFunction stringEq = new IRFunction("__mx_stringEq", true);
        stringEq.setReturnType(getIRType("char"));
        stringEq.builtinAddParameter(getIRType("string"));
        stringEq.builtinAddParameter(getIRType("string"));
        addBuiltinFunction(stringEq);

        IRFunction stringNe = new IRFunction("__mx_stringNe", true);
        stringNe.setReturnType(getIRType("char"));
        stringNe.builtinAddParameter(getIRType("string"));
        stringNe.builtinAddParameter(getIRType("string"));
        addBuiltinFunction(stringNe);

        IRFunction stringLength = new IRFunction("__mx_stringLength", true);
        stringLength.setReturnType(getIRType("int"));
        stringLength.builtinAddParameter(getIRType("string"));
        addBuiltinFunction(stringLength);

        IRFunction stringSubstring = new IRFunction("__mx_stringSubstring", true);
        stringSubstring.setReturnType(getIRType("string"));
        stringSubstring.builtinAddParameter(getIRType("string"));
        stringSubstring.builtinAddParameter(getIRType("int"));
        stringSubstring.builtinAddParameter(getIRType("int"));
        addBuiltinFunction(stringSubstring);

        IRFunction stringParseInt = new IRFunction("__mx_stringParseInt", true);
        stringParseInt.setReturnType(getIRType("int"));
        stringParseInt.builtinAddParameter(getIRType("string"));
        addBuiltinFunction(stringParseInt);

        IRFunction stringOrd = new IRFunction("__mx_stringOrd", true);
        stringOrd.setReturnType(getIRType("int"));
        stringOrd.builtinAddParameter(getIRType("string"));
        stringOrd.builtinAddParameter(getIRType("int"));
        addBuiltinFunction(stringOrd);

        IRFunction malloc = new IRFunction("__mx_malloc", true);
        malloc.setReturnType(getIRType("string"));
        malloc.builtinAddParameter(getIRType("int"));
        addBuiltinFunction(malloc);
    }

    public IRFunction getGlobalConstructor() {
        return globalConstructor;
    }

    public String getLLVMGlobalConstructors() {
        return "@llvm.global_ctors = appending global [1 x { i32, void ()*, i8* }] [{ i32, void ()*, i8* } { i32 65535, void ()* @" + globalConstructor.getFunctionName() + ", i8* null }]";
    }

    public String getGlobalInitializeFunctionName() {
        return globalInitializeFunctionName;
    }

    public IRFunction generateSingleInitializeFunction() {
        String initFuncName = globalInitializeFunctionName + "." + singleInitializeFunctions.size();
        IRFunction initFunc = new IRFunction(initFuncName);
        initFunc.setReturnType(getIRType("void"));
        singleInitializeFunctions.add(initFunc);
        return initFunc;
    }

    public IRFunction generateGlobalInitializeFunction() {
        if (generatedInitializeFunction) throw new IRError("multi-generated global init");
        generatedInitializeFunction = true;
        return new IRFunction(getGlobalInitializeFunctionName(), false);
    }

    private void addBuiltinFunction(IRFunction function) {
        if (builtinFunctions.containsKey(function.getFunctionName())) throw new IRError("duplicated IR builtin function name");
        builtinFunctions.put(function.getFunctionName(), function);
    }

    public IRFunction getBuiltinFunction(String name) {
        assert builtinFunctions.containsKey(name);
        return builtinFunctions.get(name);
    }

    public void addFunction(IRFunction function) {
        if (functions.containsKey(function.getFunctionName())) throw new IRError("duplicated IR function name");
        functions.put(function.getFunctionName(), function);
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

    public void addNewConstString(String value) {
        if (strings.containsKey(value)) return;
        log.Debugf("receive new string constant \"%s\".\n", value);
        strings.put(value, new IRConstString(getIRType("string"), value, stringCnt++));
    }

    public int getConstStringId(String tar) {
        if (strings.containsKey(tar)) return strings.get(tar).getId();
        throw new IRError("get string id failed.");
    }

    public ArrayList<IRStructureType> getClasses() {
        return classes;
    }

    public HashMap<String, IRGlobalDefine> getGlobalDefines() {
        return globalDefines;
    }

    public HashMap<String, IRConstString> getStrings() {
        return strings;
    }

    public HashMap<String, IRFunction> getFunctions() {
        return functions;
    }

    public HashMap<String, IRFunction> getBuiltinFunctions() {
        return builtinFunctions;
    }

    public ArrayList<IRFunction> getSingleInitializeFunctions() {
        return singleInitializeFunctions;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

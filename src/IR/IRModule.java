package IR;

import IR.Operand.IRConstString;
import IR.TypeSystem.IRIntType;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;
import IR.TypeSystem.IRVoidType;
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
    private final HashMap<String, IRFunction> functions = new HashMap<>();
    private final HashMap<String, IRGlobalDefine> globalDefines = new HashMap<>();
    private final HashMap<String, IRConstString> strings = new HashMap<>();
    private int stringCnt = 0;
    private final IRFunction globalConstructor;
    private final ArrayList<IRFunction> singleInitializeFunctions = new ArrayList<>();

    private boolean generatedInitializeFunction = false;
    private final String globalInitializeFunctionName = "__mx_global_init";

    public IRModule() {
        addType("void", new IRVoidType());
        addType("bool", new IRIntType(1));
        addType("char", new IRIntType(8));
        addType("int", new IRIntType(32));
        addType("string", new IRPointerType(getIRType("char")));
        addType("void *", new IRPointerType(getIRType("void")));

        globalConstructor = generateGlobalInitializeFunction();
        globalConstructor.setReturnType(getIRType("void"));
    }

    public void initializeBuiltinFunction(GlobalScope globalScope) {
        IRFunction print = new IRFunction("print", true);
        print.setReturnType(getIRType("void"));
        print.addParameterType(globalScope.getClass("string"), getIRType("string"));
        addBuiltinFunction(print);
        globalScope.getFunction("print").setIRFunction(print);

        IRFunction printInt = new IRFunction("printInt", true);
        printInt.setReturnType(getIRType("void"));
        printInt.addParameterType(globalScope.getClass("int"), getIRType("int"));
        addBuiltinFunction(printInt);
        globalScope.getFunction("printInt").setIRFunction(printInt);

        IRFunction println = new IRFunction("println", true);
        println.setReturnType(getIRType("void"));
        println.addParameterType(globalScope.getClass("string"), getIRType("string"));
        addBuiltinFunction(println);
        globalScope.getFunction("println").setIRFunction(println);

        IRFunction printlnInt = new IRFunction("printlnInt", true);
        printlnInt.setReturnType(getIRType("void"));
        printlnInt.addParameterType(globalScope.getClass("int"), getIRType("int"));
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
        toString.addParameterType(globalScope.getClass("int"), getIRType("int"));
        addBuiltinFunction(toString);
        globalScope.getFunction("toString").setIRFunction(toString);

        // todo add other member builtin functions
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
        initFunc.appendBasicBlock(initFunc.getEntryBlock());
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

    public void addFunction(IRFunction function) {
        if (functions.containsKey(function.getFunctionName())) throw new IRError("duplicated IR function name");
        functions.put(function.getFunctionName(), function);
    }

    public IRFunction getFunction(String name) {
        if (functions.containsKey(name)) return functions.get(name);
        throw new IRError("IR function not found");
    }

    public void addType(String name, IRTypeSystem type) {
        if (types.containsKey(name)) throw new IRError("duplicated IR type name");
        types.put(name, type);
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

    public IRConstString getConstString(String value) {
        assert strings.containsKey(value);
        return strings.get(value);
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

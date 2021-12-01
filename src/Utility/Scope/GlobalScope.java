package Utility.Scope;

import Utility.Cursor;
import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.Type.ClassType;
import Utility.error.SyntaxError;

import java.util.HashMap;

public class GlobalScope extends Scope {
    private final HashMap<String, ClassType> classes = new HashMap<>();

    public GlobalScope(Scope parentScope) {
        super(parentScope);

        Cursor origin = new Cursor(-1, -1);
        FunctionEntity function;

        ClassType intType = new ClassType("int");
        addClass("int", intType);
        ClassType boolType = new ClassType("bool");
        addClass("bool", boolType);
        ClassType voidType = new ClassType("void");
        addClass("void", voidType);
        ClassType nullType = new ClassType("null");
        addClass("null", nullType);

        ClassType stringType = new ClassType("string");
        stringType.setClassScope(new ClassScope(null));
        function = new FunctionEntity(new FunctionScope(intType, null), "length", origin);
        stringType.addMethod(function);
        function = new FunctionEntity(new FunctionScope(stringType, null), "substring", origin);
        function.addParameter(new VariableEntity(intType, "left", origin)).addParameter(new VariableEntity(intType, "right", origin));
        stringType.addMethod(function);
        function = new FunctionEntity(new FunctionScope(intType, null), "parseInt", origin);
        stringType.addMethod(function);
        function = new FunctionEntity(new FunctionScope(intType, null), "ord", origin);
        function.addParameter(new VariableEntity(intType, "pos", origin));
        stringType.addMethod(function);
        addClass("string", stringType);

        function = new FunctionEntity(new FunctionScope(voidType, null), "print", origin);
        function.addParameter(new VariableEntity(stringType, "str", origin));
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(voidType, null), "println", origin);
        function.addParameter(new VariableEntity(stringType, "str", origin));
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(voidType, null), "printInt", origin);
        function.addParameter(new VariableEntity(intType, "n", origin));
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(voidType, null), "printlnInt", origin);
        function.addParameter(new VariableEntity(intType, "n", origin));
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(stringType, null), "getString", origin);
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(intType, null), "getInt", origin);
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(stringType, null), "toString", origin);
        function.addParameter(new VariableEntity(intType, "i", origin));
        addFunction(function);

        // for ir
        function = new FunctionEntity(new FunctionScope(stringType, null), "__mx_concatenateString", origin);
        function.addParameter(new VariableEntity(stringType, "s1", origin));
        function.addParameter(new VariableEntity(stringType, "s2", origin));
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(boolType, null), "__mx_stringLt", origin);
        function.addParameter(new VariableEntity(stringType, "s1", origin));
        function.addParameter(new VariableEntity(stringType, "s2", origin));
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(boolType, null), "__mx_stringLe", origin);
        function.addParameter(new VariableEntity(stringType, "s1", origin));
        function.addParameter(new VariableEntity(stringType, "s2", origin));
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(boolType, null), "__mx_stringGt", origin);
        function.addParameter(new VariableEntity(stringType, "s1", origin));
        function.addParameter(new VariableEntity(stringType, "s2", origin));
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(boolType, null), "__mx_stringGe", origin);
        function.addParameter(new VariableEntity(stringType, "s1", origin));
        function.addParameter(new VariableEntity(stringType, "s2", origin));
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(boolType, null), "__mx_stringEq", origin);
        function.addParameter(new VariableEntity(stringType, "s1", origin));
        function.addParameter(new VariableEntity(stringType, "s2", origin));
        addFunction(function);

        function = new FunctionEntity(new FunctionScope(boolType, null), "__mx_stringNe", origin);
        function.addParameter(new VariableEntity(stringType, "s1", origin));
        function.addParameter(new VariableEntity(stringType, "s2", origin));
        addFunction(function);
    }

    public void addClass(String typeName, ClassType type) {
        if (classes.containsKey(typeName)) throw new SyntaxError("repeated type name", new Cursor(-1, -1));
        classes.put(typeName, type);
    }

    public HashMap<String, ClassType> getClasses() {
        return classes;
    }

    public ClassType getClass(String name) {
        return classes.get(name);
    }

    public boolean hasThisClass(String name) {
        return classes.containsKey(name);
    }
}

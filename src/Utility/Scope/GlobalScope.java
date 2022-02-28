package Utility.Scope;

import Utility.Cursor;
import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.Type.ClassType;
import Utility.error.SyntaxError;

import java.util.LinkedHashMap;

public class GlobalScope extends Scope {
    private final LinkedHashMap<String, ClassType> classes = new LinkedHashMap<>();

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
    }

    public void addClass(String typeName, ClassType type) {
        if (classes.containsKey(typeName)) throw new SyntaxError("repeated type name", new Cursor(-1, -1));
        classes.put(typeName, type);
    }

    public LinkedHashMap<String, ClassType> getClasses() {
        return classes;
    }

    public ClassType getClass(String name) {
        return classes.get(name);
    }

    public boolean hasThisClass(String name) {
        return classes.containsKey(name);
    }
}

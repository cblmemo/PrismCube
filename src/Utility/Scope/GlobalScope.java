package Utility.Scope;

import Utility.Cursor;
import Utility.Entity.FunctionEntity;
import Utility.Entity.VariableEntity;
import Utility.Type.ClassType;
import Utility.error.SyntaxError;

import java.util.HashMap;

public class GlobalScope extends Scope {
    private HashMap<String, ClassType> classes;

    public GlobalScope(Scope parentScope) {
        super(parentScope);

        addClass("int", new ClassType("int"));
        addClass("bool", new ClassType("bool"));
        addClass("string", new ClassType("string"));
        addClass("void", new ClassType("void"));
        addClass("null", new ClassType("null"));

        Cursor origin = new Cursor(-1, -1);
        FunctionEntity function;

        function = new FunctionEntity(new ClassType("void"), "print", origin);
        function.addParameter(new VariableEntity(new ClassType("string"), "str", origin));
        addFunction(function);

        function = new FunctionEntity(new ClassType("void"), "println", origin);
        function.addParameter(new VariableEntity(new ClassType("string"), "str", origin));
        addFunction(function);

        function = new FunctionEntity(new ClassType("void"), "printInt", origin);
        function.addParameter(new VariableEntity(new ClassType("int"), "n", origin));
        addFunction(function);

        function = new FunctionEntity(new ClassType("void"), "printlnInt", origin);
        function.addParameter(new VariableEntity(new ClassType("int"), "n", origin));
        addFunction(function);

        function = new FunctionEntity(new ClassType("string"), "getString", origin);
        addFunction(function);

        function = new FunctionEntity(new ClassType("int"), "getInt", origin);
        addFunction(function);

        function = new FunctionEntity(new ClassType("string"), "toString", origin);
        function.addParameter(new VariableEntity(new ClassType("int"), "i", origin));
        addFunction(function);

    }

    public void addClass(String typeName, ClassType type) {
        if (classes.containsKey(typeName)) throw new SyntaxError("repeated type name", new Cursor(-1, -1));
        classes.put(typeName, type);
    }
}

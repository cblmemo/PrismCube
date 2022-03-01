package AST.TypeNode;

import AST.ASTNode;
import IR.IRModule;
import IR.TypeSystem.IRPointerType;
import IR.TypeSystem.IRTypeSystem;
import Utility.Cursor;
import Utility.Scope.GlobalScope;
import Utility.Type.ArrayType;
import Utility.Type.ClassType;
import Utility.Type.Type;
import Utility.error.SemanticError;

import java.util.Objects;

abstract public class TypeNode extends ASTNode {
    private final String typeName;

    public TypeNode(String typeName, Cursor cursor) {
        super(cursor);
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public Type toType(GlobalScope globalScope) {
        ClassType rootElementType;
        if (getTypeName().contains("[]")) {
            rootElementType = globalScope.getClass(((ArrayTypeNode) this).getRootTypeName());
            if (rootElementType == null) throw new SemanticError("root element type doesn't exist in global scope", getCursor());
            return new ArrayType(rootElementType, ((ArrayTypeNode) this).getDimension());
        }
        rootElementType = globalScope.getClass(getTypeName());
        if (rootElementType == null) throw new SemanticError("root element type doesn't exist in global scope", getCursor());
        return rootElementType;
    }

    public IRTypeSystem toIRType(IRModule module) {
        if (this instanceof ArrayTypeNode) {
            IRTypeSystem temp = ((ArrayTypeNode) this).getElementType().toIRType(module);
            return new IRPointerType(temp);
        }
        // store class with class pointer
        if (this instanceof ClassTypeNode) return new IRPointerType(module.getIRType(typeName));
        if (this instanceof BuiltinTypeNode) {
            if (Objects.equals(getTypeName(), "int")) return IRModule.intType;
            if (Objects.equals(getTypeName(), "bool")) return IRModule.boolType;
            if (Objects.equals(getTypeName(), "string")) return IRModule.stringType;
            assert false;
        }
        return IRModule.voidType;
    }
}

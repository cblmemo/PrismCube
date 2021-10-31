package AST.TypeNode;

import AST.ASTNode;
import Utility.Cursor;
import Utility.Scope.GlobalScope;
import Utility.Type.ArrayType;
import Utility.Type.ClassType;
import Utility.Type.Type;
import Utility.error.SemanticError;

abstract public class TypeNode extends ASTNode {
    private String typeName;

    public TypeNode(String typeName, Cursor cursor) {
        super(cursor);
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public Type toType(GlobalScope globalScope) {
        ClassType rootElementType;
        if (typeName.contains("[]")) {
            rootElementType = globalScope.getClass(((ArrayTypeNode) this).getRootTypeName());
            if (rootElementType == null) throw new SemanticError("root element type doesn't exist in global scope", getCursor());
            return new ArrayType(rootElementType, ((ArrayTypeNode) this).getDimension());
        }
        rootElementType = globalScope.getClass(typeName);
        if (rootElementType == null) throw new SemanticError("root element type doesn't exist in global scope", getCursor());
        return rootElementType;
    }
}

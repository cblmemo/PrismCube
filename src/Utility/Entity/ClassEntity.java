package Utility.Entity;

import Utility.Cursor;
import Utility.Type.ClassType;

public class ClassEntity extends Entity {
    private ClassType type;

    public ClassEntity(String entityName, Cursor cursor) {
        super(entityName, cursor);
    }
}

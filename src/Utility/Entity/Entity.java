package Utility.Entity;

import Utility.Cursor;

abstract public class Entity {
    private final Cursor cursor;
    private final String entityName;

    public Entity(String entityName, Cursor cursor) {
        this.entityName = entityName;
        this.cursor = cursor;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public String getEntityName() {
        return entityName;
    }
}

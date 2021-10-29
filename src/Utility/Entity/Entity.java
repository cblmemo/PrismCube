package Utility.Entity;

import Utility.Cursor;

abstract public class Entity {
    private Cursor cursor;
    private String entityName;

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

package model.ts;

import model.er.Entity;

import java.util.ArrayList;

public abstract class EntityTransaction {
    public abstract void redo(ArrayList<Entity> entities);

    public abstract void undo(ArrayList<Entity> entities);
}

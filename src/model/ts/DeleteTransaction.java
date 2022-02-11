package model.ts;

import model.er.Entity;

import java.util.ArrayList;

public class DeleteTransaction extends AddTransaction {

    public DeleteTransaction(Entity entity) {
        super(entity);
    }

    public DeleteTransaction(Entity entity, Entity parent) {
        super(entity, parent);
    }

    @Override
    public void redo(ArrayList<Entity> entities) {
        super.undo(entities);
    }

    @Override
    public void undo(ArrayList<Entity> entities) {
        super.redo(entities);
    }
}

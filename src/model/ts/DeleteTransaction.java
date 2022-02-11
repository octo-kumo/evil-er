package model.ts;

import model.er.Attribute;
import model.er.Entity;

import java.util.ArrayList;

public class DeleteTransaction extends EntityTransaction {
    private final Entity entity;
    private Entity parent = null;

    public DeleteTransaction(Entity entity) {
        this.entity = entity;
        if (entity instanceof Attribute) parent = ((Attribute) entity).getParent();
    }

    @Override
    public void redo(ArrayList<Entity> entities) {
        if (entity instanceof Attribute && ((Attribute) entity).getParent() != null)
            (parent = ((Attribute) entity).getParent()).removeAttribute((Attribute) entity);
        else entities.remove(entity);
    }

    @Override
    public void undo(ArrayList<Entity> entities) {
        if (entity instanceof Attribute && parent != null) parent.addAttribute((Attribute) entity);
        else entities.add(entity);
    }
}

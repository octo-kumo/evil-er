package model.ts;

import model.er.Attribute;
import model.er.Entity;
import model.er.Specialization;

import java.util.ArrayList;

public class AddTransaction extends EntityTransaction {
    private final Entity entity;
    private Entity parent;

    public AddTransaction(Entity entity) {
        this(entity, entity instanceof Attribute ? ((Attribute) entity).getParent() : null);
    }

    public AddTransaction(Entity entity, Entity parent) {
        this.entity = entity;
        this.parent = parent;
    }

    @Override
    public void redo(ArrayList<Entity> entities) {
        if (entity instanceof Attribute && parent != null) parent.addAttribute((Attribute) entity);
        else entities.add(entity);

        if (entity instanceof Specialization && parent != null && ((Specialization) entity).getSuperclass() == null)
            ((Specialization) entity).setSuperclass(parent);
    }

    @Override
    public void undo(ArrayList<Entity> entities) {
        if (entity instanceof Attribute && ((Attribute) entity).getParent() != null)
            (parent = ((Attribute) entity).getParent()).removeAttribute((Attribute) entity);
        else entities.remove(entity);
    }
}

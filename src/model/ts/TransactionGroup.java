package model.ts;

import model.er.Entity;

import java.util.ArrayList;

public class TransactionGroup extends EntityTransaction {
    private final EntityTransaction[] transactions;

    public TransactionGroup(EntityTransaction... transactions) {
        this.transactions = transactions;
    }

    @Override
    public void redo(ArrayList<Entity> entities) {
        for (EntityTransaction transaction : transactions) transaction.redo(entities);
    }

    @Override
    public void undo(ArrayList<Entity> entities) {
        for (int i = transactions.length - 1; i >= 0; i--) transactions[i].undo(entities);
    }
}

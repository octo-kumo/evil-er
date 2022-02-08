package main.rs;

import model.Vector;
import model.er.Attribute;
import model.er.Entity;
import model.er.Relationship;
import model.er.Specialization;
import model.others.Pair;
import model.rs.Column;
import model.rs.Table;
import org.jetbrains.annotations.Nullable;
import utils.EnglishNoun;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Converter {
    public static void convert(ArrayList<Entity> entities, ArrayList<Table> tables) {
        HashMap<Entity, Table> tableMap = new HashMap<>();
        AtomicInteger numEntity = new AtomicInteger();
        ArrayList<Pair<Attribute, Table>> multiAttributes = new ArrayList<>();
        entities.stream().filter(entity -> entity.getClass() == Entity.class).filter(entity -> !entity.attributes.isEmpty()).forEach(entity -> {
            System.out.printf("Entity, %s%n", entity.getName());
            Table table = new Table(entity.getName());

            flatten(entity.attributes).forEach(e -> table.add(new Column(e.getName(), e.isKey())));
            entity.attributes.stream().filter(Attribute::isWeak).forEach(a -> {
                System.out.println("\tMultivalued attribute " + entity.getName() + "::" + a.getName() + ", saving for later");
                multiAttributes.add(new Pair<>(a, table));
            });

            table.set(0, numEntity.getAndIncrement() * Column.HEIGHT * 2);
            tables.add(table);
            tableMap.put(entity, table);
        });
        entities.stream().filter(entity -> entity.getClass() == Relationship.class).forEach(entity -> {
            System.out.printf("Relationship, %s, %s%n", entity.getName(), ((Relationship<?>) entity).nodes.stream().map(Entity::getName).collect(Collectors.toList()));

            int combinedInTo = findEntityToMerge((Relationship<?>) entity);
            Table table = combinedInTo == -1 ? new Table(entity.getName()) : tableMap.get(((Relationship<?>) entity).nodes.get(combinedInTo));
            if (combinedInTo != -1) System.out.printf("\tCombining into: %s%n", table.name);

            flatten(entity.attributes).forEach(e -> table.add(new Column(e.getName(), e.isKey())));
            entity.attributes.stream().filter(Attribute::isWeak).forEach(a -> multiAttributes.add(new Pair<>(a, table)));

            List<Entity> nodes = ((Relationship<?>) entity).nodes;
            List<Relationship.RelationshipSpec> specs = ((Relationship<?>) entity).specs;
            int bound = nodes.size();
            IntStream.range(0, bound).filter(i -> i != combinedInTo).forEach(i -> {
                Entity e = nodes.get(i);
                Relationship.RelationshipSpec spec = specs.get(i);
                Table found = firstIdentifiableTable(tableMap, entities, e);
                if (found != null) {
                    System.out.printf("\tAdded table, %s%n", found.name);
                    table.add(found, combinedInTo == -1 ? spec.role.isEmpty() ? found.name : spec.role : entity.getName(),
                            combinedInTo == -1 || entity.isWeak());
                }
            });

            if (combinedInTo == -1) table.set(600, Vector.average(((Relationship<?>) entity).nodes.stream()
                    .map(tableMap::get).filter(Objects::nonNull)
                    .collect(Collectors.toList())).getY());

            tables.add(table);
            tableMap.put(entity, table);
        });
        entities.stream().filter(entity -> entity.getClass() == Specialization.class).forEach(entity -> {
            System.out.printf("Specialization, %s%n", ((Specialization) entity).nodes.stream().map(Entity::getName).collect(Collectors.toList()));

            Entity sp = ((Specialization) entity).getSuperclass();
            if (tableMap.get(sp) == null) sp = findSuperclass(entities, sp);
            Table parent = tableMap.get(sp);
            if (parent == null) return;
            parent.add(new Column("type", false));

            ((Specialization) entity).getSubclasses().stream().map(tableMap::get).filter(Objects::nonNull).forEach(child -> child.add(parent, "inherits", true));
        });
        tables.forEach(Table::revalidate);
        multiAttributes.forEach(p -> {
            Table table = new Table(p.getB().name + "::" + EnglishNoun.pluralOf(p.getA().getName()));
            if (p.getA().attributes.size() > 0) // wtf composite multivalued attribute, ignoring recursive ones
                flatten(p.getA().attributes).forEach(e -> table.add(new Column(e.getName(), true)));
            else
                table.add(new Column(EnglishNoun.singularOf(p.getA().getName()), true));
            table.add(p.getB(), "attribute of", true);
            table.set(p.getB().add(p.getB().getShape().getWidth() + Column.WIDTH, 0));
            tables.add(table);
            tableMap.put(p.getA(), table);
        });
        tables.forEach(Table::revalidate);
    }

    public static @Nullable Entity findSuperclass(ArrayList<Entity> entities, Entity entity) {
        return entities.stream().filter(e -> e instanceof Specialization && ((Specialization) e).hasSubclass(entity))
                .map(e -> ((Specialization) e).getSuperclass()).findAny().orElse(null);
    }

    public static int findEntityToMerge(Relationship<?> relationship) {
        List<Relationship.RelationshipSpec> specs = relationship.specs;
        IntStream multiple = IntStream.range(0, relationship.nodes.size())
                .filter(i -> !specs.get(i).amm.isEmpty() && !Objects.equals(specs.get(i).amm, "1") && specs.get(i).total);
        int[] collect = multiple.toArray();
        if (collect.length == 1) return collect[0];
        return -1;
    }

    private static Stream<Attribute> flatten(ArrayList<Attribute> attributes) {
        return attributes.stream().filter(a -> !a.isWeak()).flatMap(a -> a.attributes.size() > 0 ? flatten(a.attributes) : Stream.of(a));
    }

    public static @Nullable Table firstIdentifiableTable(HashMap<Entity, Table> tables, ArrayList<Entity> entities, Entity entity) {
        if (entity == null) return null;
        Table table = tables.get(entity);
        if (table != null) return table;
        else return firstIdentifiableTable(tables, entities, findSuperclass(entities, entity));
    }
}

package main.rs;

import model.Vector;
import model.er.Entity;
import model.er.Relationship;
import model.er.Specialization;
import model.rs.Attribute;
import model.rs.Table;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Converter {
    public static void convert(ArrayList<Entity> entities, ArrayList<Table> tables) {
        HashMap<Entity, Table> tableMap = new HashMap<>();
        entities.stream().filter(entity -> entity.getClass() == Entity.class).filter(entity -> !entity.attributes.isEmpty()).forEach(entity -> {
            Table table = new Table(entity.getName());
            flatten(entity.attributes).forEach(e -> table.add(new Attribute(e.getName(), e.isKey())));
            table.set(Math.random() * 1000, Math.random() * 1000);
            tables.add(table);
            tableMap.put(entity, table);
        });
        entities.stream().filter(entity -> entity.getClass() == Relationship.class).forEach(entity -> {
            System.out.printf("Relationship, %s, %s%n", entity.getName(), ((Relationship<?>) entity).nodes.stream().map(Entity::getName).collect(Collectors.toList()));
            Table nTable = new Table(entity.getName());
            int combineTo = findEntityToMerge((Relationship<?>) entity);
            if (combineTo != -1) {
                Entity combineToEntity = ((Relationship<?>) entity).nodes.get(combineTo);
                if (combineToEntity != entity) {
                    System.out.println("\tCombining into " + combineTo + ": " + combineToEntity.getName());
                    nTable = tableMap.get(combineToEntity);
                }
            }

            Table table = nTable;
            flatten(entity.attributes).forEach(e -> table.add(new Attribute(e.getName(), e.isKey())));

            List<Entity> nodes = ((Relationship<?>) entity).nodes;
            for (int i = 0; i < nodes.size(); i++) {
                if (combineTo == i) continue;
                Table found = firstIdentifiableTable(tableMap, entities, nodes.get(i));
                if (found != null) {
                    System.out.println("\tAdded table, " + found.name);
                    table.add(found, entity.isWeak());
                }
            }
            table.set(Vector.average(((Relationship<?>) entity).nodes.stream()
                    .map(tableMap::get).filter(Objects::nonNull)
                    .collect(Collectors.toList())));

            tables.add(table);
            tableMap.put(entity, table);
        });
        entities.stream().filter(entity -> entity.getClass() == Specialization.class).forEach(entity -> {
            System.out.printf("Specialization, %s%n", ((Specialization) entity).nodes.stream().map(Entity::getName).collect(Collectors.toList()));

            Entity sp = ((Specialization) entity).getSuperclass();
            if (tableMap.get(sp) == null) sp = findSuperclass(entities, sp);
            Table parent = tableMap.get(sp);
            if (parent == null) return;
            ((Specialization) entity).getSubclasses().forEach(e -> {
                if (tableMap.get(e) != null) {
                    tableMap.get(e).add(parent, true);
                }
            });
        });
    }

    public static @Nullable Entity findSuperclass(ArrayList<Entity> entities, Entity entity) {
        return entities.stream().filter(e -> e instanceof Specialization && ((Specialization) e).hasSubclass(entity))
                .map(e -> ((Specialization) e).getSuperclass()).findAny().orElse(null);
    }

    public static Stream<model.er.Attribute> findIdentifiers(ArrayList<Entity> entities, Entity entity) {
        Stream<model.er.Attribute> attributeStream = entity.attributes.stream().filter(model.er.Attribute::isKey);
        Entity sp = findSuperclass(entities, entity);
        if (sp != null) attributeStream = Stream.concat(attributeStream, findIdentifiers(entities, sp));
        return attributeStream;
    }

    public static int findEntityToMerge(Relationship<?> relationship) {
        List<Relationship.RelationshipSpec> specs = relationship.specs;
        IntStream multiple = IntStream.range(0, relationship.nodes.size())
                .filter(i -> !specs.get(i).amm.isEmpty() && !Objects.equals(specs.get(i).amm, "1") && specs.get(i).total);
        int[] collect = multiple.toArray();
        if (collect.length == 1) return collect[0];
        return -1;
    }

    private static Stream<model.er.Attribute> flatten(ArrayList<model.er.Attribute> attributes) {
        return attributes.stream().flatMap(a -> a.attributes.size() > 0 ? flatten(a.attributes) : Stream.of(a));
    }

    public static @Nullable Table firstIdentifiableTable(HashMap<Entity, Table> tables, ArrayList<Entity> entities, Entity entity) {
        if (entity == null) return null;
        Table table = tables.get(entity);
        if (table != null) return table;
        else return firstIdentifiableTable(tables, entities, findSuperclass(entities, entity));
    }
}

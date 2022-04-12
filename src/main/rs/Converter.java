package main.rs;

import model.Vector;
import model.er.Attribute;
import model.er.Entity;
import model.er.Relationship;
import model.er.Specialization;
import model.rs.Column;
import model.rs.Table;
import org.jetbrains.annotations.Nullable;
import utils.EnglishNoun;
import utils.models.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Converter {
    public static void convert(ArrayList<Entity> entities, ArrayList<Table> tables) {
        try {
            HashMap<Entity, Table> tableMap = new HashMap<>();
            AtomicInteger numEntity = new AtomicInteger();
            ArrayList<Pair<Attribute, Table>> multiAttributes = new ArrayList<>();
            entities.stream().filter(entity -> entity.getClass() == Entity.class).forEach(entity -> {
                if (entity.attributes.isEmpty() && findSuperclass(entities, entity) != null) return;
                System.out.printf("Entity, %s%n", entity.getName());
                Table table = new Table(entity.getName());

                flatten(entity.attributes).forEach(e -> table.add(new Column(e.getName(), e.isKey(), e.isUnique(), e.getDataType(), e.getDataParam())));
                entity.attributes.stream().filter(Attribute::isWeak).forEach(a -> {
                    System.out.println("\tMultivalued attribute " + entity.getName() + "::" + a.getName() + ", saving for later");
                    multiAttributes.add(new Pair<>(a, table));
                });

                table.set(0, numEntity.getAndIncrement() * Column.HEIGHT * 2);
                tables.add(table);
                tableMap.put(entity, table);
            });
            entities.stream().filter(entity -> entity.getClass() == Relationship.class).forEach(entity -> {
                System.out.printf("Relationship, %s, %s%n", entity.getName(), ((Relationship) entity).nodes.stream().map(Entity::getName).collect(Collectors.toList()));

                int combinedInTo = findEntityToMerge((Relationship) entity);

                Table table;
                if (combinedInTo == -1) table = new Table(entity.getName());
                else {
                    Entity sp = ((Relationship) entity).nodes.get(combinedInTo);
                    table = tableMap.get(sp);
                    if (table == null) table = tableMap.get(findSuperclass(entities, sp));
                }
                if (combinedInTo != -1) System.out.printf("\tCombining into: %s%n", table.getName());

                Table finalTable = table;
                flatten(entity.attributes).forEach(e -> finalTable.add(new Column(e.getName(), e.isKey(), e.isUnique(), e.getDataType(), e.getDataParam())));
                entity.attributes.stream().filter(Attribute::isWeak).forEach(a -> multiAttributes.add(new Pair<>(a, finalTable)));

                List<Entity> nodes = ((Relationship) entity).nodes;
                List<Relationship.RelationshipSpec> specs = ((Relationship) entity).specs;
                int bound = nodes.size();
                IntStream.range(0, bound).filter(i -> i != combinedInTo).forEach(i -> {
                    Entity e = nodes.get(i);
                    Relationship.RelationshipSpec spec = specs.get(i);
                    Table found = firstIdentifiableTable(tableMap, entities, e);
                    if (found != null) {
                        System.out.printf("\tAdded table, %s%n", found.getName());
                        finalTable.add(found, combinedInTo == -1 ? spec.role.isEmpty() ? found.getName() : spec.role : entity.getName(), combinedInTo == -1 || entity.isWeak());
                    }
                });

                if (combinedInTo == -1) {
                    table.set(600, Vector.average(((Relationship) entity).nodes.stream().map(tableMap::get).filter(Objects::nonNull).collect(Collectors.toList())).getY());
                    tables.add(table);
                }
                tableMap.put(entity, table);
            });
            entities.stream().filter(entity -> entity.getClass() == Specialization.class).forEach(entity -> {
                System.out.printf("Specialization, %s%n", ((Specialization) entity).nodes.stream().map(Entity::getName).collect(Collectors.toList()));

                Entity sp = ((Specialization) entity).getSuperclass();
                if (tableMap.get(sp) == null) sp = findSuperclass(entities, sp);
                Table parent = tableMap.get(sp);
                if (parent == null) return;
                parent.add(new Column("type", false, Column.DataType.VARCHAR, "16"));

                ((Specialization) entity).getSubclasses().stream().map(tableMap::get).filter(Objects::nonNull).forEach(child -> child.add(parent, "inherits", true));
            });
            tables.forEach(Table::revalidate);
            multiAttributes.forEach(p -> {
                Table table = new Table(p.b.getName() + "::" + EnglishNoun.pluralOf(p.a.getName()));
                if (p.a.attributes.size() > 0) // wtf composite multivalued attribute, ignoring recursive ones
                    flatten(p.a.attributes).forEach(e -> table.add(new Column(e.getName(), true, e.isUnique(), e.getDataType(), e.getDataParam())));
                else table.add(new Column(EnglishNoun.singularOf(p.a.getName()), true));
                table.add(p.b, "attribute of", true);
                table.set(p.b.add(p.b.getShape().getWidth() + Column.WIDTH, 0));
                tables.add(table);
                tableMap.put(p.a, table);
            });
            tables.forEach(Table::revalidate);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static @Nullable Entity findSuperclass(ArrayList<Entity> entities, Entity entity) {
        return entities.stream().filter(e -> e instanceof Specialization && ((Specialization) e).hasSubclass(entity)).map(e -> ((Specialization) e).getSuperclass()).findAny().orElse(null);
    }

    public static int findEntityToMerge(Relationship relationship) {
        List<Relationship.RelationshipSpec> specs = relationship.specs;
        IntStream multiple = IntStream.range(0, relationship.nodes.size()).filter(i -> !specs.get(i).amm.isEmpty() && !Objects.equals(specs.get(i).amm, "1") && specs.get(i).total);
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

    public static String convertToSQLInsert(ArrayList<Entity> entities, String did) {
        StringBuilder builder = new StringBuilder();
        builder.append("USE evilEr;\n");
        entities.stream().sorted(Comparator.comparing(e -> getWeight(e.getClass()))).forEach(e -> convertEntityToSQL(builder, e, did, 0));
        return builder.toString();
    }

    public static int getWeight(Class<? extends Entity> c) {
        if (Entity.class.equals(c)) return 0;
        else if (Attribute.class.equals(c)) return 1;
        else if (Relationship.class.equals(c)) return 2;
        else if (Specialization.class.equals(c)) return 3;
        return 4;
    }

    public static void convertEntityToSQL(StringBuilder builder, Entity e, String did, int depth) {
        builder.append("INSERT INTO object VALUES (").append(e.getX()).append(", ").append(e.getY()).append(", '").append(e.getID()).append("', '").append(e.getName()).append("', '").append(e.getClass().getSimpleName().toLowerCase()).append("', ").append(e.isWeak()).append(", '").append(did).append("');\n");
        if (e instanceof Specialization) {
            Specialization s = (Specialization) e;
            builder.append(new String(new char[depth]).replace("\0", "  ")).append("INSERT INTO specialization VALUES (").append(s.isDisjoint()).append(", '").append(s.getID()).append("', '").append(did).append("');\n");
        }
        if (e instanceof Attribute) {
            Attribute a = (Attribute) e;
            builder.append(new String(new char[depth]).replace("\0", "  ")).append("INSERT INTO attribute VALUES (").append(a.isKey()).append(", ").append(a.isDerived()).append(", '").append(a.getParent().getID()).append("', '").append(a.getID()).append("', '").append(did).append("');\n");
        }
        if (e instanceof Relationship) {
            Relationship r = (Relationship) e;
            for (int i = 0; i < r.nodes.size(); i++) {
                Relationship.RelationshipSpec s = r.specs.get(i);
                Entity o = r.nodes.get(i);
                builder.append(new String(new char[depth + 1]).replace("\0", "  ")).append("INSERT INTO relates VALUES ('").append(s.role).append("', ").append(s.total).append(", '").append(s.amm).append("', '").append(e.getID()).append("', '").append(o.getID()).append("', '").append(did).append("');\n");
            }
        }
        for (Attribute attribute : e.attributes) {
            convertEntityToSQL(builder, attribute, did, depth + 1);
        }
        builder.append(new String(new char[depth]).replace("\0", "  ")).append("# ================ #\n");
    }
}

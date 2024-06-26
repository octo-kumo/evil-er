package model.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import model.er.Attribute;
import model.er.Entity;
import model.er.Relationship;
import model.er.Specialization;
import model.rs.Table;
import shapes.lines.RelationLine;
import utils.Prompts;

import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Serializer {
    public static final Gson gson;
    public static final Gson clean;
    public static final Gson specialize;
    public static final Gson attribute;
    public static final Gson relationship;
    public static final Gson specialization;
    private static final Type ARRAYLIST_ENTITY = new TypeToken<ArrayList<Entity>>() {
    }.getType();
    private static final Type ARRAYLIST_TABLE = new TypeToken<ArrayList<Table>>() {
    }.getType();

    static {
        GsonBuilder builder = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Table.class, new TableSerializer())
                .registerTypeAdapter(Attribute.class, new AttributeSerializer())
                .registerTypeAdapter(Entity.class, new TypedEntitySerializer())
                .registerTypeAdapter(Specialization.class, new SpecializationSerializer())
                .registerTypeAdapter(Relationship.class, new RelationshipSerializer());
        gson = builder.create();

        builder = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Attribute.class, new AttributeSerializer())
                .registerTypeAdapter(Specialization.class, new SpecializationSerializer())
                .registerTypeAdapter(Relationship.class, new RelationshipSerializer());
        specialize = builder.create();

        builder = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Attribute.class, new AttributeSerializer());
        attribute = builder.create();

        builder = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Relationship.class, new RelationshipSerializer());
        relationship = builder.create();

        builder = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Specialization.class, new SpecializationSerializer());
        specialization = builder.create();

        builder = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation();
        clean = builder.create();
    }

    public static String serializeTables(List<Table> tables) {
        return gson.toJson(tables);
    }

    public static void serializeTables(List<Table> tables, Writer writer) {
        gson.toJson(tables, writer);
    }

    public static String serialize(List<Entity> entities) {
        return gson.toJson(entities);
    }

    public static void serialize(List<Entity> entities, Writer writer) {
        gson.toJson(entities, writer);
    }

    public static ArrayList<Entity> deserialize(String json) {
        ArrayList<Entity> entities = gson.fromJson(json, ARRAYLIST_ENTITY);
        postprocess(entities);
        return entities;
    }

    public static ArrayList<Entity> deserialize(JsonReader json) {
        ArrayList<Entity> entities = gson.fromJson(json, ARRAYLIST_ENTITY);
        postprocess(entities);
        return entities;
    }

    public static ArrayList<Table> deserializeTables(JsonReader json) {
        ArrayList<Table> tables = gson.fromJson(json, ARRAYLIST_TABLE);
        try {
            postprocessTables(tables);
        } catch (Exception e) {
            Prompts.report(e);
        }
        return tables;
    }

    private static void postprocess(ArrayList<Entity> entities) {
        for (Entity o : entities) {
            Entity.updateParents(o);
            if (o instanceof Relationship) { // inflate relationships
                Relationship r = (Relationship) o;
                r.nodes = Arrays.stream(r._nodes).map(n ->
                        entities.stream().filter(e -> n.equals(e.getName())).findAny().orElse(null)
                ).collect(Collectors.toList());
                r.lines = IntStream.range(0, r.specs.size()).mapToObj(i ->
                        new RelationLine(r, r.nodes.get(i), r.specs.get(i))
                ).collect(Collectors.toList());
                r.revalidate();
            }
        }
        VersionUpgrade.upgradeEntities(entities);
    }

    private static void postprocessTables(ArrayList<Table> tables) {
        for (Table t : tables) {
            Table.updateParents(t);
            t._foreign.forEach(r ->
                    t.add(tables.stream().filter(ot ->
                            ot.getName().equals(r.table)).findAny().orElse(null), r.role, r.required, r.prefix));
        }
        tables.forEach(Table::revalidate);
        VersionUpgrade.upgradeTables(tables);
    }
}

package model.serializers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.entities.Attribute;
import model.entities.Entity;
import model.entities.Relationship;
import model.lines.RelationLine;

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

    static {
        GsonBuilder builder = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Attribute.class, new AttributeSerializer())
                .registerTypeAdapter(Entity.class, new TypedEntitySerializer())
                .registerTypeAdapter(Relationship.class, new RelationshipSerializer());
        gson = builder.create();

        builder = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Attribute.class, new AttributeSerializer())
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
                .excludeFieldsWithoutExposeAnnotation();
        clean = builder.create();
    }

    public static String serialize(List<Entity> entities) {
        return gson.toJson(entities);
    }

    public static ArrayList<Entity> deserialize(String json) {
        ArrayList<Entity> entities = gson.fromJson(json, new TypeToken<ArrayList<Entity>>() {
        }.getType());
        // fill relationships
        for (Entity o : entities) {
            Entity.updateParents(o);
            if (o instanceof Relationship) { // inflate relationships
                Relationship<Entity> r = (Relationship<Entity>) o;
                r.nodes = Arrays.stream(r._nodes).map(n ->
                        entities.stream().filter(e -> n.equals(e.getName())).findAny().orElse(null)
                ).collect(Collectors.toList());
                r.lines = IntStream.range(0, r.specs.size()).mapToObj(i ->
                        new RelationLine<>(r, r.nodes.get(i), r.specs.get(i))
                ).collect(Collectors.toList());
            }
        }
        return entities;
    }
}

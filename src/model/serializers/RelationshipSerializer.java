package model.serializers;

import com.google.gson.*;
import model.er.Entity;
import model.er.Relationship;

import java.lang.reflect.Type;

public class RelationshipSerializer implements JsonSerializer<Relationship>, JsonDeserializer<Relationship> {
    @Override
    public JsonElement serialize(Relationship src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = Serializer.attribute.toJsonTree(src).getAsJsonObject();
        root.add("nodes", Serializer.clean.toJsonTree(src.nodes.stream().map(Entity::getName).toArray(String[]::new)));
        root.addProperty("type", "relationship");
        return root;
    }

    @Override
    public Relationship deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject relationObject = json.getAsJsonObject();
        Relationship rel = Serializer.attribute.fromJson(relationObject, Relationship.class);
        rel._nodes = Serializer.clean.fromJson(relationObject.get("nodes"), String[].class);
        return rel;
    }
}

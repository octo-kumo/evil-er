package model.serializers;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import model.entities.Entity;
import model.entities.Relationship;

import java.lang.reflect.Type;

public class RelationshipSerializer implements JsonSerializer<Relationship<? extends Entity>>, JsonDeserializer<Relationship<? extends Entity>> {
    @Override
    public JsonElement serialize(Relationship<? extends Entity> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = Serializer.attribute.toJsonTree(src).getAsJsonObject();
        root.add("nodes", Serializer.clean.toJsonTree(src.nodes.stream().map(Entity::getName).toArray(String[]::new)));
        root.addProperty("type", "relationship");
        return root;
    }

    @Override
    public Relationship<? extends Entity> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject relationObject = json.getAsJsonObject();
        Relationship<? extends Entity> rel = Serializer.attribute.fromJson(relationObject, new TypeToken<Relationship<? extends Entity>>() {
        }.getType());
        rel._nodes = Serializer.clean.fromJson(relationObject.get("nodes"), String[].class);
        return rel;
    }
}

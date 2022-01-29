package model.serializers;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import model.entities.Attribute;
import model.entities.Entity;
import model.entities.Relationship;
import model.entities.Specialization;

import java.lang.reflect.Type;

public class SpecializationSerializer implements JsonSerializer<Specialization>, JsonDeserializer<Specialization> {
    @Override
    public JsonElement serialize(Specialization src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = Serializer.relationship.toJsonTree(src).getAsJsonObject();
        root.add("nodes", Serializer.clean.toJsonTree(src.nodes.stream().map(Entity::getName).toArray(String[]::new)));
        root.addProperty("type", "specialization");
        return root;
    }

    @Override
    public Specialization deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject relationObject = json.getAsJsonObject();
        Specialization rel = Serializer.attribute.fromJson(relationObject, Specialization.class);
        rel._nodes = Serializer.clean.fromJson(relationObject.get("nodes"), String[].class);
        return rel;
    }
}

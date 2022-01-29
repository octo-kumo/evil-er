package model.serializers;

import com.google.gson.*;
import model.entities.Attribute;
import model.entities.Entity;
import model.entities.Relationship;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TypedEntitySerializer implements JsonSerializer<Entity>, JsonDeserializer<Entity> {
    private final Map<String, Class<? extends Entity>> typeRegistry;

    {
        typeRegistry = new HashMap<>();
        typeRegistry.put("entity", Entity.class);
        typeRegistry.put("relationship", Relationship.class);
        typeRegistry.put("attribute", Attribute.class);
    }

    @Override
    public Entity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject animalObject = json.getAsJsonObject();
        Class<? extends Entity> animalType = typeRegistry.get(animalObject.get("type").getAsString());
        return Serializer.specialize.fromJson(animalObject, animalType);
    }

    @Override
    public JsonElement serialize(Entity src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = Serializer.attribute.toJsonTree(src).getAsJsonObject();
        root.addProperty("type", "entity");
        return root;
    }
}

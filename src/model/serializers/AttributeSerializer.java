package model.serializers;

import com.google.gson.*;
import model.er.Attribute;

import java.lang.reflect.Type;

public class AttributeSerializer implements JsonSerializer<Attribute>, JsonDeserializer<Attribute> {
    @Override
    public JsonElement serialize(Attribute src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = Serializer.clean.toJsonTree(src).getAsJsonObject();
        root.addProperty("type", "attribute");
        return root;
    }

    @Override
    public Attribute deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject animalObject = json.getAsJsonObject();
        return Serializer.clean.fromJson(animalObject, Attribute.class);
    }
}

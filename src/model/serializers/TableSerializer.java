package model.serializers;

import com.google.gson.*;
import model.rs.Table;
import utils.models.Tuple;

import java.lang.reflect.Type;
import java.util.stream.Collectors;

public class TableSerializer implements JsonSerializer<Table>, JsonDeserializer<Table> {
    @Override
    public JsonElement serialize(Table src, Type typeOfSrc, JsonSerializationContext context) {
        src._foreign = src.foreign.stream().map(t -> new Tuple<>(t.a, t.b, t.c.getName())).collect(Collectors.toList());
        return Serializer.clean.toJsonTree(src).getAsJsonObject();
    }

    @Override
    public Table deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        return Serializer.clean.fromJson(object, Table.class);
    }
}

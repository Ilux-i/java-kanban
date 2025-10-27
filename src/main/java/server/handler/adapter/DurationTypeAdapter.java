package main.java.server.handler.adapter;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;

public class DurationTypeAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
    @Override
    public JsonElement serialize(Duration duration, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(duration.toString());
    }

    @Override
    public Duration deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return Duration.parse(json.getAsString());
        } catch (Exception e) {
            throw new JsonParseException("Invalid duration format: " + json.getAsString(), e);
        }
    }
}

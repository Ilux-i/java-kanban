package main.java.server.handler.adapter;

import com.google.gson.*;
import main.java.task.SubTask;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;

public class SubTaskTypeAdapter implements JsonSerializer<SubTask>, JsonDeserializer<SubTask> {

    private final Gson gson;

    public SubTaskTypeAdapter() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
    }

    @Override
    public JsonElement serialize(SubTask subTask, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", subTask.getId());
        jsonObject.addProperty("name", subTask.getName());
        jsonObject.addProperty("description", subTask.getDescription());
        jsonObject.addProperty("status", subTask.getStatus().toString());
        jsonObject.addProperty("idMaster", subTask.getMaster());

        if (subTask.getDuration() != null) {
            jsonObject.add("duration", gson.toJsonTree(subTask.getDuration()));
            jsonObject.add("startTime", gson.toJsonTree(subTask.getStartTime()));
            jsonObject.add("endTime", gson.toJsonTree(subTask.getEndTime()));
        } else {
            jsonObject.add("duration", JsonNull.INSTANCE);
            jsonObject.add("startTime", JsonNull.INSTANCE);
            jsonObject.add("endTime", JsonNull.INSTANCE);
        }

        return jsonObject;
    }

    @Override
    public SubTask deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        if (!jsonObject.has("name")) {
            throw new JsonParseException("Missing required field: name");
        }
        if (!jsonObject.has("description")) {
            throw new JsonParseException("Missing required field: description");
        }
        if (!jsonObject.has("idMaster")) {
            throw new JsonParseException("Missing required field: idMaster");
        }

        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();
        long idMaster = jsonObject.get("idMaster").getAsLong();

        SubTask subTask;

        if (jsonObject.has("duration")) {

            Duration duration = gson.fromJson(jsonObject.get("duration"), Duration.class);
            LocalDateTime startTime = gson.fromJson(jsonObject.get("startTime"), LocalDateTime.class);
            subTask = new SubTask(name, description, idMaster, duration, startTime);
        } else {
            subTask = new SubTask(name, description, idMaster);
        }

        if (jsonObject.has("id")) {
            long id = jsonObject.get("id").getAsLong();
            if (id != 0) {
                subTask.setId(id);
            }
        }

        if (jsonObject.has("status")) {
            String statusStr = jsonObject.get("status").getAsString();
            try {
                subTask.setStatus(main.java.status.TaskStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Invalid status value: " + statusStr);
            }
        }

        return subTask;
    }
}

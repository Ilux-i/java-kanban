package main.java.server.handler.adapter;

import com.google.gson.*;
import main.java.task.Epic;
import main.java.task.SubTask;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class EpicTypeAdapter implements JsonSerializer<Epic>, JsonDeserializer<Epic> {

    private final Gson gson;

    public EpicTypeAdapter() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(SubTask.class, new SubTaskTypeAdapter())
                .create();
    }

    @Override
    public JsonElement serialize(Epic epic, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", epic.getId());
        jsonObject.addProperty("name", epic.getName());
        jsonObject.addProperty("description", epic.getDescription());
        jsonObject.addProperty("status", epic.getStatus().toString());

        if (epic.getDuration() != null) {
            jsonObject.add("duration", gson.toJsonTree(epic.getDuration()));
            jsonObject.add("startTime", gson.toJsonTree(epic.getStartTime()));
            jsonObject.add("endTime", gson.toJsonTree(epic.getEndTime()));
        } else {
            jsonObject.add("duration", JsonNull.INSTANCE);
            jsonObject.add("startTime", JsonNull.INSTANCE);
            jsonObject.add("endTime", JsonNull.INSTANCE);
        }

        JsonArray subtasksArray = new JsonArray();
        for (SubTask subTask : epic.getSubtasks()) {
            subtasksArray.add(gson.toJsonTree(subTask));
        }
        jsonObject.add("subtasks", subtasksArray);

        return jsonObject;
    }

    @Override
    public Epic deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject data = json.getAsJsonObject();

        String name = data.get("name").getAsString();
        String description = data.get("description").getAsString();

        Epic epic;

        if (data.has("duration")) {
            Duration duration = gson.fromJson(data.get("duration"), Duration.class);
            LocalDateTime startTime = gson.fromJson(data.get("startTime"), LocalDateTime.class);
            epic = new Epic(name, description, duration, startTime);
        } else {
            epic = new Epic(name, description);
        }

        if (data.has("id")) {
            epic.setId(data.get("id").getAsLong());
        }

        if (data.has("status")) {
            epic.setStatus(main.java.status.TaskStatus.valueOf(
                    data.get("status").getAsString()));
        }

        if (data.has("subtasks")) {
            JsonArray subtasksArray = data.getAsJsonArray("subtasks");
            ArrayList<SubTask> subtasks = new ArrayList<>();

            for (JsonElement element : subtasksArray) {
                SubTask subTask = gson.fromJson(element, SubTask.class);
                subtasks.add(subTask);
            }
            epic.setSubtasks(subtasks);
        } else {
            epic.setSubtasks(new ArrayList<>());
        }

        return epic;
    }
}

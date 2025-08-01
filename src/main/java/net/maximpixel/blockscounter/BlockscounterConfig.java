package net.maximpixel.blockscounter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

public class BlockscounterConfig {

    public static final BlockscounterConfig INSTANCE = new BlockscounterConfig();

    private final File file;
    private boolean stop = true, hideHud = false;

    private BlockscounterConfig() {
        file = new File("config/blockscounter.json");
        refresh();
    }

    public synchronized boolean isStop() {
        return stop;
    }

    public synchronized void setStop(boolean stop) {
        this.stop = stop;
        save();
    }

    public synchronized boolean isHideHud() {
        return hideHud;
    }

    public synchronized void setHideHud(boolean hideHud) {
        this.hideHud = hideHud;
        save();
    }

    public synchronized void save() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("stop", stop);
        jsonObject.addProperty("hideHud", hideHud);

        try (JsonWriter jsonWriter = new JsonWriter(new FileWriter(file))) {
            Gson gson = new Gson();
            gson.toJson(jsonObject, jsonWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void refresh() {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(new FileReader(file), JsonObject.class);

            try {
                stop = jsonObject.has("stop") ? jsonObject.get("stop").getAsBoolean() : true;
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                hideHud = jsonObject.has("hideHud") ? jsonObject.get("hideHud").getAsBoolean() : false;
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

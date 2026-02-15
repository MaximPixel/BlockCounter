package net.maximpixel.blockscounter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Session {

    private static final Gson GSON = new Gson();
    public static Session INSTANCE = new Session(new File("workSessions/session.bs"));
    private final File file;
    private long placed = 0, broken = 0;

    public Session(File file) {
        this.file = file;

        File parentFile = file.getParentFile();

        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        refresh();
    }

    public static Session getInstance() {
        return INSTANCE;
    }

    private static long getPlacedByFile(File file) {
        try {
            return Files.lines(file.toPath())
                    .filter(a -> {
                        try {
                            JsonArray jsonArray = GSON.fromJson(a, JsonArray.class);

                            return jsonArray.get(4).getAsJsonPrimitive().getAsInt() == 1;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static long getBrokenByFile(File file) {
        try {
            return Files.lines(file.toPath())
                    .filter(a -> {
                        try {
                            JsonArray jsonArray = GSON.fromJson(a, JsonArray.class);

                            return jsonArray.get(4).getAsJsonPrimitive().getAsInt() == 2;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long getPlaced() {
        return placed;
    }

    public long getBroken() {
        return broken;
    }

    public synchronized void refresh() {
        placed = getPlacedByFile(file);
        broken = getBrokenByFile(file);
    }

    public synchronized void open() {
        if (!file.getParentFile().exists()) {
            return;
        }

        try {
            Util.getOperatingSystem().open(file.getParentFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
        return property.name((T) value);
    }

    private JsonObject stateToJsonObject(BlockState state) {
        JsonObject jsonObject = new JsonObject();

        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getEntries()
                .entrySet()) {
            var property = entry.getKey();
            jsonObject.addProperty(property.getName(), nameValue(property, entry.getValue()));
        }

        return jsonObject;
    }

    private void addDimensionToJsonArray(JsonArray jsonArray, RegistryEntry<DimensionType> dimension) {
        if (dimension == null) {
            jsonArray.add(0);
            return;
        }

        String id = dimension.getIdAsString();

        if ("minecraft:overworld".equals(id)) {
            jsonArray.add(1);
        } else if ("minecraft:the_nether".equals(id)) {
            jsonArray.add(2);
        } else if ("minecraft:the_end".equals(id)) {
            jsonArray.add(3);
        } else {
            jsonArray.add(id);
        }
    }

    public synchronized void writePlaced(BlockPos pos, RegistryEntry<DimensionType> dimension, BlockState placementState, BlockState replacedState) {
        if (placementState == null) {
            return;
        }

        placed++;

        try (FileOutputStream fileOutputStream = new FileOutputStream(file, true)) {
            JsonArray jsonArray = new JsonArray();

            jsonArray.add(pos.getX());
            jsonArray.add(pos.getY());
            jsonArray.add(pos.getZ());
            addDimensionToJsonArray(jsonArray, dimension);
            jsonArray.add(1);
            jsonArray.add(placementState.getRegistryEntry().getIdAsString());
            jsonArray.add(stateToJsonObject(placementState));
            if (!replacedState.isAir()) {
                jsonArray.add(replacedState.getRegistryEntry().getIdAsString());
                jsonArray.add(stateToJsonObject(replacedState));
            }
            jsonArray.add(Instant.now().getEpochSecond());

            fileOutputStream.write((GSON.toJson(jsonArray) + "\n").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void writeBroken(BlockPos pos, RegistryEntry<DimensionType> dimension, BlockState brokenState) {
        if (brokenState == null) {
            return;
        }

        broken++;

        try (FileOutputStream fileOutputStream = new FileOutputStream(file, true)) {
            JsonArray jsonArray = new JsonArray();

            jsonArray.add(pos.getX());
            jsonArray.add(pos.getY());
            jsonArray.add(pos.getZ());
            addDimensionToJsonArray(jsonArray, dimension);
            jsonArray.add(2);
            jsonArray.add(brokenState.getRegistryEntry().getIdAsString());
            jsonArray.add(stateToJsonObject(brokenState));
            jsonArray.add(Instant.now().getEpochSecond());

            fileOutputStream.write((GSON.toJson(jsonArray) + "\n").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void saveSession() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

        String timestamp = now.format(formatter);

        File renameTo = new File("workSessions/session-" + timestamp + ".bs");

        boolean result = file.renameTo(renameTo);

        MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION,
                Text.translatable("blocksCounter.toast.sessionSaved"), Text.translatable("blocksCounter.toast.sessionSavedAs", renameTo.getName())));

        refresh();
    }
}

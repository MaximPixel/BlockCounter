package net.maximpixel.blockscounter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.maximpixel.blockscounter.BlockscounterConfig;
import net.maximpixel.blockscounter.Session;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.function.Supplier;

public class BlockscounterClient implements ClientModInitializer {

    public static final KeyBinding.Category KEYS_CATEGORY =
            new KeyBinding.Category(Identifier.of("blockscounter", "category"));
    public static KeyBinding openScreenKey;

    public static long prevPlaced = -1;
    public static long prevBroken = -1;
    public static int placedCooldown = 0;
    public static int brokenCooldown = 0;
    public static int stopedCooldown = 0;

    public static void onPlace(ItemPlacementContext context, Supplier<BlockState> placementStateSupplier) {
        try {
            if (context.getWorld().isClient()) {
                if (BlockscounterConfig.INSTANCE.isStop()) {
                    return;
                }

                BlockState placementState = placementStateSupplier.get();
                BlockPos blockPos = context.getBlockPos();

                if (context.canPlace()) {
                    Session.getInstance()
                            .writePlaced(blockPos, context.getWorld().getDimensionEntry(), placementState, context.getWorld().getBlockState(blockPos));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        if (world.isClient()) {
            if (BlockscounterConfig.INSTANCE.isStop()) {
                return;
            }

            RegistryEntry<DimensionType> dimensionEntry = world instanceof World ? ((World) world).getDimensionEntry() : null;

            Session.getInstance().writeBroken(pos, dimensionEntry, state);
        }
    }

    @Override
    public void onInitializeClient() {
        openScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.blocksСounter.openScreen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KEYS_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreenKey.isPressed()) {
                if (client.player != null) {
                    client.setScreen(new BlockcounterScreen());
                }
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            boolean isStop = BlockscounterConfig.INSTANCE.isStop();

            if (isStop) {
                stopedCooldown++;
            } else {
                stopedCooldown = 0;
            }

            if (client.options.hudHidden || stopedCooldown > 1000) {
                return;
            }

            Session session = Session.getInstance();

            if (prevPlaced != session.getPlaced()) {
                if (prevPlaced != -1) {
                    placedCooldown = 10;
                }
                prevPlaced = session.getPlaced();
            }

            if (prevBroken != session.getBroken()) {
                if (prevBroken != -1) {
                    brokenCooldown = 10;
                }
                prevBroken = session.getBroken();
            }

            if (BlockscounterConfig.INSTANCE.isHideHud()) {
                placedCooldown = 0;
                brokenCooldown = 0;
                return;
            }

            if (placedCooldown > 0) {
                placedCooldown--;
            }
            if (brokenCooldown > 0) {
                brokenCooldown--;
            }

            ArrayList<Text> lines = new ArrayList<>();

            if (isStop) {
                lines.add(Text.translatable("blocksCounter.sessionStopped")
                        .styled(style -> style.withColor(Formatting.RED)));
            } else {
                lines.add(Text.translatable("blocksCounter.sessionStarted")
                        .styled(style -> style.withColor(Formatting.GREEN)));
            }

            {
                int r = 255 - (int) ((float) placedCooldown / 10F * 255F);
                int g = 255;
                int b = 255 - (int) ((float) placedCooldown / 10F * 255F);

                lines.add(Text.translatable("blocksCounter.placed")
                        .append(Text.literal(String.valueOf(session.getPlaced()))
                                .styled(style -> style.withColor((r << 16) | (g << 8) | b))));
            }

            {
                int r = 255;
                int g = 255 - (int) ((float) brokenCooldown / 10F * 255F);
                int b = 255 - (int) ((float) brokenCooldown / 10F * 255F);

                lines.add(Text.translatable("blocksCounter.broken")
                        .append(Text.literal(String.valueOf(session.getBroken()))
                                .styled(style -> style.withColor((r << 16) | (g << 8) | b))));
            }

            for (int i = 0; i < lines.size(); i++) {
                drawContext.drawText(client.textRenderer,
                        lines.get(i),
                        client.getWindow().getScaledWidth() - client.textRenderer.getWidth(lines.get(i)) - 10,
                        10 + 12 * i,
                        0xFFFFFFFF,
                        false);
            }
        });

    }
}

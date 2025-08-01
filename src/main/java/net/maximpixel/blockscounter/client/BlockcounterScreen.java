package net.maximpixel.blockscounter.client;

import net.maximpixel.blockscounter.BlockscounterConfig;
import net.maximpixel.blockscounter.Session;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BlockcounterScreen extends Screen {
    public BlockcounterScreen() {
        super(Text.translatable("gui.blocksCounter.title"));
    }

    @Override
    protected void init() {
        super.init();

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.blocksCounter.startStop"), button -> {
            BlockscounterConfig.INSTANCE.setStop(!BlockscounterConfig.INSTANCE.isStop());
        }).dimensions((width - 150) / 2, (height - 20) / 2 - 44, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.blocksCounter.startNewSession"), button -> {
            Session.getInstance().saveSession();
        }).dimensions((width - 150) / 2, (height - 20) / 2 - 22, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.blocksCounter.openFolder"), button -> {
            Session.getInstance().open();
        }).dimensions((width - 150) / 2, (height - 20) / 2, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.blocksCounter.refresh"), button -> {
            Session.getInstance().refresh();
        }).dimensions((width - 150) / 2, (height - 20) / 2 + 22, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.blocksCounter.toggleHud"), button -> {
            BlockscounterConfig.INSTANCE.setHideHud(!BlockscounterConfig.INSTANCE.isHideHud());
        }).dimensions((width - 150) / 2, (height - 20) / 2 + 44, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawText(
                textRenderer,
                Text.translatable(
                        BlockscounterConfig.INSTANCE.isStop()
                                ? "blocksCounter.statusStopped"
                                : "blocksCounter.statusRunning"
                ).styled(style -> style.withColor(
                        BlockscounterConfig.INSTANCE.isStop() ? Formatting.RED : Formatting.GREEN
                )),
                (width - 150) / 2,
                10 + 12 * 0,
                0xFFFFFF,
                true
        );

        context.drawText(
                textRenderer,
                Text.translatable("blocksCounter.placed").append(String.valueOf(Session.getInstance().getPlaced())),
                (width - 150) / 2,
                10 + 12 * 1,
                0xFFFFFF,
                true
        );

        context.drawText(
                textRenderer,
                Text.translatable("blocksCounter.broken").append(String.valueOf(Session.getInstance().getBroken())),
                (width - 150) / 2,
                10 + 12 * 2,
                0xFFFFFF,
                true
        );
    }
}

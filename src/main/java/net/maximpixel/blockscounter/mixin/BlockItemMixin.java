package net.maximpixel.blockscounter.mixin;

import net.maximpixel.blockscounter.client.BlockscounterClient;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin extends Item {
    public BlockItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;")
    public void place(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        BlockscounterClient.onPlace(context, () -> getPlacementState(getPlacementContext(context)));
    }

    @Shadow
    public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
        return null;
    }

    @Shadow
    protected BlockState getPlacementState(ItemPlacementContext context) {
        return null;
    }
}

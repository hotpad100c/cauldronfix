package mypals.ml.mixin;

import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.block.BlockColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockColors.class)
public class BlockColorsMixin {
    @Inject(method = "create", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void cauldron_dyeing(CallbackInfoReturnable<BlockColors> cir, BlockColors blockColors) {
        blockColors.registerColorProvider((state, world, pos, tintIndex) -> world != null && pos != null ? CauldronFix.getColor(world, pos) : -1, ModBlocks.COLORED_CAULDRON);
        blockColors.registerColorProvider((state, world, pos, tintIndex) -> world != null && pos != null ? CauldronFix.getColor(world, pos) : -1, ModBlocks.POTION_CAULDRON);
    }
}

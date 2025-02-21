package mypals.ml.mixin;

import com.llamalad7.mixinextras.sugar.Cancellable;
import mypals.ml.CauldronBlockWatcher;
import mypals.ml.CauldronFix;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {

	@Inject( at = @At("HEAD"), method = "neighborUpdate")
    private void mixinNeighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, WireOrientation wireOrientation, boolean notify, CallbackInfo ci) {
		CauldronBlockWatcher.cauldronBlockCheck(world, pos);
	}

}

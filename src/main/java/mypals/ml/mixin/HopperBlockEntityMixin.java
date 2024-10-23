package mypals.ml.mixin;

import net.fabricmc.fabric.mixin.networking.client.accessor.MinecraftClientAccessor;
import net.minecraft.block.BlockState;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static mypals.ml.HopperTransportManage.HopperTransportManager.hopperTransfer;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    @Shadow
    public int transferCooldown;

    @Inject(method = {"serverTick"}, at = {@At(value = "HEAD")})
    private static void serverTick(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {
        // 获取漏斗上方的方块位置

        int updatedCooldown = hopperTransfer(blockEntity.transferCooldown, world, pos, state, blockEntity, ci);
        if(updatedCooldown > 0)
            ((HopperBlockEntityAccessor) blockEntity).invokeSetTransferCooldown(updatedCooldown);
    }
}

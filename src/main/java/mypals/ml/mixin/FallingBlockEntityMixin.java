package mypals.ml.mixin;


import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.block.Block;

import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static mypals.ml.CauldronBlockWatcher.anvilLandEvent;

@Mixin(FallingBlockEntity.class)
abstract class FallingBlockEntityMixin extends Entity {
    @Shadow
    private BlockState block;
    @Unique
    private float anvilFallDistance;

    public FallingBlockEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    ordinal = 0,
                    target = "Lnet/minecraft/entity/FallingBlockEntity;getWorld()Lnet/minecraft/world/World;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void anvilPerFallOnGround(CallbackInfo ci, Block block) {
        if (this.getWorld().isClient) return;
        if (this.isOnGround()) return;
        this.anvilFallDistance = this.fallDistance;
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    ordinal = 10,
                    target = "Lnet/minecraft/entity/FallingBlockEntity;getWorld()Lnet/minecraft/world/World;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void anvilFallOnGround(CallbackInfo ci, Block block, BlockPos blockPos) {
        if (this.getWorld().isClient) return;
        if (!this.block.isIn(BlockTags.ANVIL)) return;

        anvilLandEvent(fallDistance, blockPos, this.getWorld());
    }
}
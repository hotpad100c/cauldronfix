package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeveledCauldronBlock.class)
public class LeveledCauldronBlockMixin {
    @WrapMethod(method = "onEntityCollision")
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, Operation<Void> original) {
        if(state.getBlock() instanceof AbstractCauldronBlock abstractCauldronBlock){
            if (((AbstractCauldronBlockAccessor) abstractCauldronBlock).invokeIsEntityTouchingFluid(state, pos, entity)) {
                if(state.getBlock() == Blocks.POWDER_SNOW_CAULDRON){
                    if (!(entity instanceof LivingEntity)) {
                        if (world.isClient) {
                            Random random = world.getRandom();
                            boolean bl = entity.lastRenderX != entity.getX() || entity.lastRenderZ != entity.getZ();
                            if (bl && random.nextBoolean()) {
                                world.addParticle(
                                        ParticleTypes.SNOWFLAKE,
                                        entity.getX(),
                                        (double)(pos.getY() + 1),
                                        entity.getZ(),
                                        (double)(MathHelper.nextBetween(random, -1.0F, 1.0F) * 0.083333336F),
                                        0.05F,
                                        (double)(MathHelper.nextBetween(random, -1.0F, 1.0F) * 0.083333336F)
                                );
                            }
                        }
                    }
                    entity.slowMovement(state, new Vec3d(0.9F, 1.5, 0.9F));
                    entity.setInPowderSnow(true);
                }else if(state.getBlock() == Blocks.WATER_CAULDRON){
                    entity.fallDistance = 0;
                }
            }
        }
        original.call(state,world,pos,entity);
    }
}

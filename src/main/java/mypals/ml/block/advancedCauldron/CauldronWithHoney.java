package mypals.ml.block.advancedCauldron;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CauldronWithHoney extends LeveledCauldronBlock {

    public static final IntProperty LEVEL = Properties.LEVEL_3;

    public CauldronWithHoney(Biome.Precipitation precipitation, CauldronBehavior.CauldronBehaviorMap behaviorMap, Settings settings, Biome.Precipitation precipitation1) {
        super(precipitation, behaviorMap, settings);
        this.setDefaultState((this.stateManager.getDefaultState()));
    }
    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (this.isEntityTouchingFluid(state, pos, entity) && entity instanceof LivingEntity livingEntity) {
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,100,2));
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION,100,2));
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE,100,2));
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION,100,2));

            if((livingEntity.getStatusEffects().contains(StatusEffects.POISON) || livingEntity.getStatusEffects().contains(StatusEffects.WITHER)||livingEntity.getStatusEffects().contains(StatusEffects.HUNGER)));
            {
                livingEntity.removeStatusEffect(StatusEffects.POISON);
                livingEntity.removeStatusEffect(StatusEffects.WITHER);
                livingEntity.removeStatusEffect(StatusEffects.HUNGER);
                //CauldronFix.decrementFluidLevel(state, world, pos, true, 1);
            }


            Vec3d vec3d = new Vec3d(0.25, 0.25, 0.25);
            entity.slowMovement(state, vec3d);

        }
    }



    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(LEVEL);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected void fillFromDripstone(BlockState state, World world, BlockPos pos, Fluid fluid) {
        if (this.isFull(state)) {
            return;
        }
        BlockState blockState = state.with(LEVEL, state.get(LEVEL) + 1);
        world.setBlockState(pos, blockState);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
        world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_DRIPS_WATER_INTO_CAULDRON, pos, 0);
    }
    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(!player.isCreative() && !player.isSpectator()) {
            this.angerNearbyBees(world, pos);
            Criteria.BEE_NEST_DESTROYED.trigger((ServerPlayerEntity) player, state, tool, 0);
        }
        world.updateComparators(pos, this);
    }

    private void angerNearbyBees(World world, BlockPos pos) {
        Box box = new Box(pos).expand(8.0, 6.0, 8.0);
        List<BeeEntity> list = world.getNonSpectatingEntities(BeeEntity.class, box);
        if (!list.isEmpty()) {
            List<PlayerEntity> list2 = world.getNonSpectatingEntities(PlayerEntity.class, box);
            if (list2.isEmpty()) {
                return;
            }
            for (BeeEntity beeEntity : list) {
                if (beeEntity.getTarget() != null) continue;
                PlayerEntity playerEntity = Util.getRandom(list2, world.random);
                beeEntity.setTarget(playerEntity);
            }
        }
    }
}

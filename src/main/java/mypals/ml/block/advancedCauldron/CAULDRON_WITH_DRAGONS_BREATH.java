package mypals.ml.block.advancedCauldron;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

import java.util.Optional;
import java.util.stream.Stream;

public class CAULDRON_WITH_DRAGONS_BREATH extends LeveledCauldronBlock {
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 3;
    public static final IntProperty LEVEL = Properties.LEVEL_3;
    private static final int BASE_FLUID_HEIGHT = 6;
    private static final double FLUID_HEIGHT_PER_LEVEL = 3.0;

    public CAULDRON_WITH_DRAGONS_BREATH(Biome.Precipitation precipitation, CauldronBehavior.CauldronBehaviorMap behaviorMap, Settings settings, Biome.Precipitation precipitation1) {
        super(precipitation, behaviorMap, settings);
        this.setDefaultState((this.stateManager.getDefaultState()).with(LEVEL, 1));
    }
    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (this.isEntityTouchingFluid(state, pos, entity)) {
            entity.damage(world.getDamageSources().dragonBreath(), 2.5f);
            if(entity instanceof LivingEntity livingEntity)
            {
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,100,5));
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED,100,5));
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST,100,2));
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE,100,3));
            }

        }
    }


    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(LEVEL)*4;
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
}

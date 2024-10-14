package mypals.ml.block.advancedCauldron;

import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;

public class CauldronWithDragonsBreath extends LeveledCauldronBlock {
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 3;
    public static final IntProperty LEVEL = Properties.LEVEL_3;
    private static final int BASE_FLUID_HEIGHT = 6;
    private static final double FLUID_HEIGHT_PER_LEVEL = 3.0;

    public CauldronWithDragonsBreath(Biome.Precipitation precipitation, CauldronBehavior.CauldronBehaviorMap behaviorMap, Settings settings, Biome.Precipitation precipitation1) {
        super(precipitation, behaviorMap, settings);
        this.setDefaultState((this.stateManager.getDefaultState()).with(LEVEL, 1));
    }
    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        this.spawnBreakParticles(world, player, pos, state);

        if(!player.isCreative() && !player.isSpectator()) {
            AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(world, pos.toCenterPos().getX(), pos.toCenterPos().getY(), pos.toCenterPos().getZ());
            areaEffectCloudEntity.setParticleType(ParticleTypes.DRAGON_BREATH);
            areaEffectCloudEntity.setDuration(this.getStateManager().getDefaultState().get(LEVEL) * 200);
            areaEffectCloudEntity.setRadius(this.getStateManager().getDefaultState().get(LEVEL));
            areaEffectCloudEntity.setRadiusGrowth((7.0f - areaEffectCloudEntity.getRadius()) / (float) areaEffectCloudEntity.getDuration());
            areaEffectCloudEntity.addEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1){
                @Override
                public void onEntityDamage(LivingEntity livingEntity, DamageSource source, float amount){
                    livingEntity.damage(world.getDamageSources().dragonBreath(),amount);
                }
            });
            world.spawnEntity(areaEffectCloudEntity);
        }

        world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state));
        return state;
    }
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(world, pos.toCenterPos().getX(), pos.toCenterPos().getY(), pos.toCenterPos().getZ());
        areaEffectCloudEntity.setParticleType(ParticleTypes.DRAGON_BREATH);
        areaEffectCloudEntity.setDuration(this.getStateManager().getDefaultState().get(LEVEL) * 200);
        areaEffectCloudEntity.setRadius(this.getStateManager().getDefaultState().get(LEVEL));
        areaEffectCloudEntity.setRadiusGrowth((7.0f - areaEffectCloudEntity.getRadius()) / (float) areaEffectCloudEntity.getDuration());
        areaEffectCloudEntity.addEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1){
            @Override
            public void onEntityDamage(LivingEntity livingEntity, DamageSource source, float amount){
                livingEntity.damage(world.getDamageSources().dragonBreath(),amount);
            }
        });
        world.spawnEntity(areaEffectCloudEntity);
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

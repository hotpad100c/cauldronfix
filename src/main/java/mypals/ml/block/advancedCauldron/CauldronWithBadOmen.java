package mypals.ml.block.advancedCauldron;

import mypals.ml.CauldronFix;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.tick.OrderedTick;

import java.util.List;

public class CauldronWithBadOmen extends LeveledCauldronBlock {

    public static final IntProperty LEVEL = Properties.LEVEL_3;

    public CauldronWithBadOmen(Biome.Precipitation precipitation, CauldronBehavior.CauldronBehaviorMap behaviorMap, Settings settings, Biome.Precipitation precipitation1) {
        super(precipitation, behaviorMap, settings);
        this.setDefaultState((this.stateManager.getDefaultState()));
    }
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient) {
            ((ServerWorld) world).getBlockTickScheduler().scheduleTick(OrderedTick.create(this, pos));
        }
    }
    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient) {

            if (entity instanceof LivingEntity livingEntity && world.getDifficulty() != Difficulty.PEACEFUL) {
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.RAID_OMEN, 1, 1));
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 600, 4));
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 600, 4));
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 600, 2));
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 600, 1));
            }
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
        if (!world.isClient) {

            Vec3d c1 = new Vec3d(pos.add(-5, -5, -5).getX(),pos.add(-5, -5, -5).getY(),pos.add(-5, -5, -5).getZ());
            Vec3d c2 = new Vec3d(pos.add(5, 5, 5).getX(),pos.add(5, 5, 5).getY(),pos.add(5, 5, 5).getZ());

            Box box = new Box(c1, c2); // 10x10x10 范围
            List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(ServerPlayerEntity.class, box, player -> !player.isSpectator());

            if (nearbyPlayers.isEmpty()) {
                return;
            }


            ServerPlayerEntity randomPlayer = nearbyPlayers.get(random.nextInt(nearbyPlayers.size()));

            if (world.getDifficulty() != Difficulty.PEACEFUL && world.isNearOccupiedPointOfInterest(randomPlayer.getBlockPos())) {

                Raid raid = world.getRaidAt(randomPlayer.getBlockPos());

                if (raid == null || raid.getBadOmenLevel() < raid.getMaxAcceptableBadOmenLevel()) {
                    randomPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RAID_OMEN, 1, 5));
                    randomPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 600, 4));
                    randomPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 600, 4));
                    randomPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 600, 1));
                    randomPlayer.setStartRaidPos(randomPlayer.getBlockPos());
                    RaidManager manager = world.getRaidManager();
                    manager.startRaid(randomPlayer,pos);
                    CauldronFix.decrementFluidLevel(state,world,pos);
                }
            }
        }

        world.getBlockTickScheduler().scheduleTick(OrderedTick.create(this, pos));
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

    }
}

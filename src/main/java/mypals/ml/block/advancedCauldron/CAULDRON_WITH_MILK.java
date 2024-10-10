package mypals.ml.block.advancedCauldron;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

public class CAULDRON_WITH_MILK extends LeveledCauldronBlock {

    public static final IntProperty LEVEL = Properties.LEVEL_3;

    public CAULDRON_WITH_MILK(Biome.Precipitation precipitation, CauldronBehavior.CauldronBehaviorMap behaviorMap, Settings settings, Biome.Precipitation precipitation1) {
        super(precipitation, behaviorMap, settings);
        this.setDefaultState((this.stateManager.getDefaultState()).with(LEVEL, 1));
    }
    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {if (this.isEntityTouchingFluid(state, pos, entity) && entity instanceof LivingEntity livingEntity)
        {
            if (livingEntity.clearStatusEffects() && (!(livingEntity instanceof PlayerEntity) || !((PlayerEntity) livingEntity).isCreative())) {
                world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            }
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

}

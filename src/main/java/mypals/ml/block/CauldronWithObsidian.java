package mypals.ml.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class CauldronWithObsidian extends FallingBlock {
    private static final float FALLING_BLOCK_ENTITY_DAMAGE_MULTIPLIER = 2.0f;
    private static final int FALLING_BLOCK_ENTITY_MAX_DAMAGE = 40;
    public CauldronWithObsidian(Settings settings) {
        super(settings);
    }
    public static final MapCodec<CauldronWithObsidian> CODEC = CauldronWithObsidian.createCodec(CauldronWithObsidian::new);
    @Override
    protected MapCodec<? extends FallingBlock> getCodec() {
        return CODEC;
    }

    @Override
    public void onLanding(World world, BlockPos pos, BlockState fallingBlockState, BlockState currentStateInPos, FallingBlockEntity fallingBlockEntity) {

    }
    @Override
    public DamageSource getDamageSource(Entity attacker) {
        return attacker.getDamageSources().fallingAnvil(attacker);
    }
    @Override
    public int getColor(BlockState state, BlockView world, BlockPos pos) {
        return state.getMapColor((BlockView)world, (BlockPos)pos).color;
    }




    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        world.scheduleBlockTick(pos, this, this.getFallDelay());
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }


    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005f);
        if(EnchantmentHelper.getLevel(world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH) , tool) == 0) {
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            world.spawnEntity(new ItemEntity(world,pos.getX(),pos.getY(),pos.getZ(),new ItemStack(Items.OBSIDIAN,1)));
        }
        else {
            world.spawnEntity(new ItemEntity(world,pos.getX(),pos.getY(),pos.getZ(),new ItemStack(ModBlocks.CAULDRON_WITH_OBSIDIAN,1)));
        }
    }
    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
        world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.CAULDRON,1)));
    }
    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }
    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return 30;
    }

}

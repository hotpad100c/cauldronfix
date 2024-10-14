package mypals.ml.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class CauldronWithHalfCobblestone extends Block {

    public CauldronWithHalfCobblestone(Settings settings) {
        super(settings);
    }
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(
                VoxelShapes.cuboid(0, 0.1875, 0, 0.125, 1, 1),
                VoxelShapes.cuboid(0.125, 0.1875, 0.125, 0.875, 0.25, 0.875),
                VoxelShapes.cuboid(0.875, 0.1875, 0, 1, 1, 1),
                VoxelShapes.cuboid(0.125, 0.1875, 0, 0.875, 1, 0.125),
                VoxelShapes.cuboid(0.125, 0.1875, 0.875, 0.875, 1, 1),
                VoxelShapes.cuboid(0, 0, 0, 0.25, 0.1875, 0.125),
                VoxelShapes.cuboid(0, 0, 0.125, 0.125, 0.1875, 0.25),
                VoxelShapes.cuboid(0.75, 0, 0, 1, 0.1875, 0.125),
                VoxelShapes.cuboid(0.875, 0, 0.125, 1, 0.1875, 0.25),
                VoxelShapes.cuboid(0, 0, 0.875, 0.25, 0.1875, 1),
                VoxelShapes.cuboid(0, 0, 0.75, 0.125, 0.1875, 0.875),
                VoxelShapes.cuboid(0.75, 0, 0.875, 1, 0.1875, 1),
                VoxelShapes.cuboid(0.875, 0, 0.75, 1, 0.1875, 0.875),
                VoxelShapes.cuboid(0.125, 0.25, 0.125, 0.875, 0.5625, 0.875)
        );
    }
    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005f);
        if(EnchantmentHelper.getLevel(world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH) , tool) == 0) {
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.COBBLESTONE_SLAB,1)));
        }
        else {
            world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(ModBlocks.CAULDRON_WITH_HALF_COBBLE_STONE,1)));
        }
    }
    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.COBBLESTONE_SLAB,1)));
        world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.CAULDRON,1)));
    }
    @Override
    protected boolean hasComparatorOutput(BlockState state) {return true;}
    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return 5;
    }
}

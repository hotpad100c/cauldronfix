package mypals.ml.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CauldronWithEmber extends Block implements Equipment {
    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }

    private int age = 5;
    public CauldronWithEmber(Settings settings) {
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
            world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.COAL,3)));
        }
        else {
            world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(ModBlocks.CAULDRON_WITH_EMBER,1)));
        }
    }
    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        age--;
        if(age <= 0)
        {
            world.setBlockState(pos,ModBlocks.CAULDRON_WITH_COBBLE_STONE.getDefaultState());
        }
    }
    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(stack.getItem() instanceof ShovelItem)
        {
            world.setBlockState(pos,ModBlocks.CAULDRON_WITH_HALF_COBBLE_STONE.getDefaultState());
            player.swingHand(player.getActiveHand());
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
            return ItemActionResult.SUCCESS;
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.bypassesSteppingEffects() && entity instanceof LivingEntity) {
            entity.damage(world.getDamageSources().hotFloor(), 1.0f);
        }
        super.onSteppedOn(world, pos, state, entity);
    }
    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.COAL,3)));
        world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.CAULDRON,1)));
    }
    @Override
    protected boolean hasComparatorOutput(BlockState state) {return true;}
    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return 6;
    }
}

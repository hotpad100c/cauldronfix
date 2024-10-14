package mypals.ml.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class CauldronWithCobblestone extends Block {
    public CauldronWithCobblestone(Settings settings) {
        super(settings);
    }
    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005f);
        if(EnchantmentHelper.getLevel(world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH) , tool) == 0) {
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.COBBLESTONE,1)));
        }
        else {
            world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(ModBlocks.CAULDRON_WITH_COBBLE_STONE,1)));
        }
    }
    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.COBBLESTONE,1)));
        world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.CAULDRON,1)));
    }
    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }
    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return 10;
    }
    @Override
    protected void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {

        ItemStack heldItem = player.getStackInHand(player.getActiveHand());

        if (heldItem.isOf(Items.MACE)) {
            if (world.isClient) {
                player.swingHand(player.getActiveHand());
            }

            if (!world.isClient) {
                world.setBlockState(pos,ModBlocks.CAULDRON_WITH_GRAVEL.getDefaultState());
                world.playSound(null, pos, SoundEvents.ITEM_MACE_SMASH_GROUND, SoundCategory.BLOCKS, 1f, 1f);
                world.addBlockBreakParticles(pos.up(),state);
            }
        }

    }
}

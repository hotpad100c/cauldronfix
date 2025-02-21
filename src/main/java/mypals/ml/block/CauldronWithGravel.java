package mypals.ml.block;

import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class CauldronWithGravel extends Block implements EquipmentSlotProvider {
    public CauldronWithGravel(Settings settings) {
        super(settings);
    }
    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005f);
        if(EnchantmentHelper.getLevel(world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH) , tool) == 0) {
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.GRAVEL,new Random(114514).nextInt(1,3))));
        }
        else {
            world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(ModBlocks.CAULDRON_WITH_GRAVEL,1)));
        }
    }
    @Override
    public void onDestroyedByExplosion(ServerWorld world, BlockPos pos, Explosion explosion) {
        world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.GRAVEL,new Random(114514).nextInt(1,3))));
        world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.CAULDRON,1)));
    }
    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }
    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return 3;
    }
    @Override
    protected void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {

        ItemStack heldItem = player.getStackInHand(player.getActiveHand());

        if (heldItem.isOf(Items.MACE)) {
            if (world.isClient) {
                player.swingHand(player.getActiveHand());
            }

            if (!world.isClient) {
                world.setBlockState(pos,Blocks.CAULDRON.getDefaultState());
                world.playSound(null, pos, SoundEvents.ITEM_MACE_SMASH_GROUND, SoundCategory.BLOCKS, 1f, 1f);
                world.addBlockBreakParticles(pos.up(),state);
                world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.GRAVEL,new Random(114514).nextInt(1,3))));
            }
        }
    }
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(stack.getItem() instanceof ShovelItem)
        {
            world.setBlockState(pos,Blocks.CAULDRON.getDefaultState());
            world.addBlockBreakParticles(pos,state);
            player.swingHand(player.getActiveHand());
            world.playSound(null, pos, SoundEvents.BLOCK_SAND_BREAK, SoundCategory.BLOCKS, 1f, 1f);
            world.spawnEntity(new ItemEntity(world,pos.toCenterPos().getX(),pos.toCenterPos().getY(),pos.toCenterPos().getZ(),new ItemStack(Items.GRAVEL,new Random(114514).nextInt(1,3))));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public EquipmentSlot getPreferredEquipmentSlot(LivingEntity entity, ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

}

package mypals.ml;

import mypals.ml.block.ModBlocks;
import mypals.ml.block.advancedCauldron.CAULDRON_WITH_DRAGONS_BREATH;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

public class CauldronBlockWatcher {


    public static void cauldronBlockCheckWithItem(World world, BlockPos blockPos, ItemStack item, PlayerEntity player, Hand hand, CallbackInfoReturnable<ItemActionResult> cir) {
        if (!world.isClient()) {
            BlockState posUp = world.getBlockState(blockPos.up());
            BlockState pos = world.getBlockState(blockPos);

            //checkForCustomCauldrons(world, blockPos, item, player, hand, cir);

            if (item.getItem() == Items.LAVA_BUCKET &&
                    (
                            pos.getBlock().equals(Blocks.CAULDRON)||
                                    pos.getBlock().equals(Blocks.WATER_CAULDRON)||
                                        pos.getBlock().equals(Blocks.POWDER_SNOW_CAULDRON)
                    ) && isWaterBlock(posUp)){

                world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_OBSIDIAN.getDefaultState(), Block.NOTIFY_ALL);
                world.addParticle(ParticleTypes.LAVA, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.5, 0.0);
                world.playSound(null, blockPos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
                //replaceItemOnHand(player,hand, new ItemStack(Items.BUCKET));
                //cir.cancel();
                cir.setReturnValue(ItemActionResult.success(world.isClient()));
            } else if (item.getItem() == Items.WATER_BUCKET && (
                    (
                            pos.getBlock().equals(Blocks.CAULDRON)||
                                    pos.getBlock().equals(Blocks.POWDER_SNOW_CAULDRON)||
                                        pos.getBlock().equals(Blocks.LAVA_CAULDRON)
                    ) && isLavaBlock(posUp))) {
                world.addParticle(ParticleTypes.SMOKE, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.5, 0.0);
                world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_STONE.getDefaultState(), Block.NOTIFY_ALL);
                world.playSound(null, blockPos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
                //replaceItemOnHand(player,hand, new ItemStack(Items.BUCKET));
                //cir.cancel();
                cir.setReturnValue(ItemActionResult.success(world.isClient()));
            } else if (item.getItem() == Items.POWDER_SNOW_BUCKET && (
                    (
                            pos.getBlock().equals(Blocks.CAULDRON)||
                                    pos.getBlock().equals(Blocks.WATER_CAULDRON)||
                                        pos.getBlock().equals(Blocks.LAVA_CAULDRON)
                    ) && (isWaterBlock(posUp)||isLavaBlock(posUp)))){
                world.setBlockState(blockPos, Blocks.WATER_CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
                world.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.0, 0.0);
                world.playSound(null, blockPos, SoundEvents.ENTITY_PLAYER_SPLASH, SoundCategory.BLOCKS, 1f, 1f);
                cauldronBlockCheck(world, blockPos);
                //replaceItemOnHand(player,hand, new ItemStack(Items.BUCKET));
                //cir.cancel();
                cir.setReturnValue(ItemActionResult.success(world.isClient()));
            }
        }
    }

    public static void checkForCustomCauldrons(World world, BlockPos blockPos, ItemStack item, PlayerEntity player, Hand hand, CallbackInfoReturnable<ItemActionResult> cir)
    {
        if(item.getItem() == Items.DRAGON_BREATH && world.getBlockState(blockPos).getBlock().equals(Blocks.CAULDRON)){
            world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_DRAGONS_BREATH.getDefaultState(), Block.NOTIFY_ALL);
            //replaceItemOnHand(player,hand, new ItemStack(Items.GLASS_BOTTLE));
            cir.setReturnValue(ItemActionResult.SUCCESS);
        }
    }
    public static void cauldronBlockCheck(World world, BlockPos blockPos) {
        if (!world.isClient()) {
            BlockState posUp = world.getBlockState(blockPos.up());
            BlockState posDown = world.getBlockState(blockPos.down());
            BlockState pos = world.getBlockState(blockPos);

            // 确保当前方块和上方方块是锅，并根据不同类型进行处理

            if (isLavaAndWaterInteraction(pos, posUp, posDown)) {
                world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_OBSIDIAN.getDefaultState(), Block.NOTIFY_ALL);
                world.addParticle(ParticleTypes.LAVA, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.5, 0.0);
                world.playSound(null, blockPos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
            } else if (isWaterAndLavaInteraction(pos, posUp, posDown) == 1 || isWaterAndLavaInteraction(pos, posUp, posDown) == 2) {
                if (isWaterAndLavaInteraction(pos, posUp, posDown) == 1) {
                    if (getCauldronLevel(pos) == 3) {
                        world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_STONE.getDefaultState(), Block.NOTIFY_ALL);
                        world.addParticle(ParticleTypes.SMOKE, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.5, 0.0);
                        world.playSound(null, blockPos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
                    } else if (getCauldronLevel(pos) == 2) {
                        world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_COBBLE_STONE.getDefaultState(), Block.NOTIFY_ALL);
                        world.addParticle(ParticleTypes.SMOKE, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.5, 0.0);
                        world.playSound(null, blockPos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
                    } else if (getCauldronLevel(pos) == 1) {
                        world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_HALF_COBBLE_STONE.getDefaultState(), Block.NOTIFY_ALL);
                        world.addParticle(ParticleTypes.SMOKE, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.5, 0.0);
                        world.playSound(null, blockPos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
                    }
                }
                if (isWaterAndLavaInteraction(pos, posUp, posDown) == 2) {
                    if (getCauldronLevel(posDown) == 3) {
                        world.addParticle(ParticleTypes.SMOKE, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.5, 0.0);
                        world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_STONE.getDefaultState(), Block.NOTIFY_ALL);
                        world.playSound(null, blockPos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
                    } else if (getCauldronLevel(posDown) == 2) {
                        world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_COBBLE_STONE.getDefaultState(), Block.NOTIFY_ALL);
                        world.addParticle(ParticleTypes.SMOKE, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.5, 0.0);
                        world.playSound(null, blockPos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
                    } else if (getCauldronLevel(posDown) == 1) {
                        world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_HALF_COBBLE_STONE.getDefaultState(), Block.NOTIFY_ALL);
                        world.addParticle(ParticleTypes.SMOKE, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.5, 0.0);
                        world.playSound(null, blockPos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
                    }
                }
            } else if (isLavaOrWaterAndPowderSnowInteraction(pos, posUp, posDown)) {
                world.setBlockState(blockPos, Blocks.WATER_CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
                world.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.0, 0.0);
                world.playSound(null, blockPos, SoundEvents.BLOCK_POWDER_SNOW_BREAK, SoundCategory.BLOCKS, 1f, 1f);
                cauldronBlockCheck(world, blockPos);
            }
        }

    }


    // Lava 和 Water Cauldron 的交互判断
    private static boolean isLavaAndWaterInteraction(BlockState pos, BlockState posUp, BlockState posDown) {
        return (pos.getBlock().equals(Blocks.LAVA_CAULDRON) && isWaterBlock(posUp)) ||
                (posDown.getBlock().equals(Blocks.LAVA_CAULDRON) && isWaterBlock(pos));
    }
    private static boolean isLavaOrWaterAndPowderSnowInteraction(BlockState pos, BlockState posUp, BlockState posDown) {
        return (pos.getBlock().equals(Blocks.POWDER_SNOW_CAULDRON) && (isWaterBlock(posUp)||isLavaBlock(posUp))) ||
                (posDown.getBlock().equals(Blocks.POWDER_SNOW_CAULDRON) && (isWaterBlock(pos)||isLavaBlock(pos)));
    }

    // Water 和 Lava Cauldron 的交互判断
    private static Integer isWaterAndLavaInteraction(BlockState pos, BlockState posUp, BlockState posDown) {
        if (pos.getBlock().equals(Blocks.WATER_CAULDRON)) {
            if (isLavaBlock(posUp)) {
                return 1;
            }
        }

        else if (posDown.getBlock().equals(Blocks.WATER_CAULDRON))
        {
            if( isLavaBlock(pos))
            {
                return 2;
            }
        }
        else {
            return 0;
        }
        return 0;
    }

    // 判断锅是否满
    private static Integer getCauldronLevel(BlockState pos) {

        return pos.get(Properties.LEVEL_3);
    }

    // 判断是否为水方块
    private static boolean isWaterBlock(BlockState state) {
        return state.equals(Blocks.WATER.getDefaultState()) || state.getFluidState().isIn(FluidTags.WATER);
    }

    // 判断是否为熔岩方块
    private static boolean isLavaBlock(BlockState state) {
        return state.equals(Blocks.LAVA.getDefaultState()) || state.getFluidState().isIn(FluidTags.LAVA);
    }
    private static void replaceItemOnHand(PlayerEntity player,Hand hand, ItemStack item)
    {
        if(!player.isCreative() && !player.isSpectator()) {
            if (hand == Hand.MAIN_HAND)
                player.setStackInHand(Hand.MAIN_HAND, item);
            else if (hand == Hand.OFF_HAND)
                player.setStackInHand(Hand.OFF_HAND, item);
        }
    }
}

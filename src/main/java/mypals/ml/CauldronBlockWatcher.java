package mypals.ml;

import mypals.ml.block.ModBlocks;
import mypals.ml.block.advancedCauldron.CauldronWithDragonsBreath;
import net.minecraft.block.*;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class CauldronBlockWatcher {


    public static void cauldronBlockCheckWithItem(World world, BlockPos blockPos, ItemStack item, PlayerEntity player, Hand hand, CallbackInfoReturnable<ItemActionResult> cir) {
        if (!world.isClient()) {
            BlockState posUp = world.getBlockState(blockPos.up());
            BlockState pos = world.getBlockState(blockPos);

            if (item.getItem() == Items.LAVA_BUCKET &&
                    (
                            pos.getBlock().equals(Blocks.CAULDRON)||
                                    pos.getBlock().equals(Blocks.WATER_CAULDRON)||
                                        pos.getBlock().equals(Blocks.POWDER_SNOW_CAULDRON)
                    ) && isWaterBlock(posUp)){

                world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_OBSIDIAN.getDefaultState(), Block.NOTIFY_ALL);
                world.addParticle(ParticleTypes.LAVA, blockPos.getX(), blockPos.up().getY(), blockPos.getZ(), 0.0, 0.5, 0.0);
                world.playSound(null, blockPos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
                replaceItemOnHand(player,hand, new ItemStack(Items.BUCKET));
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
                replaceItemOnHand(player,hand, new ItemStack(Items.BUCKET));
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
                replaceItemOnHand(player,hand, new ItemStack(Items.BUCKET));
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
            //Dragon's breath
            else if(isLavaAndCauldronInteractionSimple(ModBlocks.CAULDRON_WITH_DRAGONS_BREATH,pos,posUp,posDown)){
                world.createExplosion(null,blockPos.up().getX(),blockPos.up().getY(),blockPos.up().getZ(), 4, World.ExplosionSourceType.BLOCK);
                world.setBlockState(blockPos, Blocks.LAVA_CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
            }
            else if(isWaterAndCauldronInteractionSimple(ModBlocks.CAULDRON_WITH_DRAGONS_BREATH,pos,posUp,posDown)){
                AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(world, blockPos.up().toBottomCenterPos().getX(), blockPos.up().toBottomCenterPos().getY(), blockPos.up().toBottomCenterPos().getZ());

                areaEffectCloudEntity.setParticleType(ParticleTypes.DRAGON_BREATH);
                areaEffectCloudEntity.setDuration(100);
                areaEffectCloudEntity.setRadius(1);
                areaEffectCloudEntity.setRadiusGrowth((7.0f - areaEffectCloudEntity.getRadius()) / (float) areaEffectCloudEntity.getDuration());
                areaEffectCloudEntity.addEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1){
                        @Override public void onEntityDamage(LivingEntity livingEntity, DamageSource source, float amount){
                            livingEntity.damage(world.getDamageSources().dragonBreath(),amount);
                        }
                });
                world.spawnEntity(areaEffectCloudEntity);
                world.playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS, 1f, 1f);


                BlockState cauldronBlockState = Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3);
                world.setBlockState(blockPos, cauldronBlockState, Block.NOTIFY_ALL);
            }//Empty cauldron
            else if (isLavaAndCauldronInteractionSimple(Blocks.CAULDRON, pos,posUp,posDown)) {
                Thread scanThread = new Thread(() -> {
                    BlockPos sourcePos = FluidTracker.findLavaFluidSource(world, blockPos.up());

                    if (sourcePos != null) {
                        world.playSound(null, blockPos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1f, 1f);
                        world.setBlockState(FluidTracker.findLavaFluidSource(world,blockPos.up()), Blocks.AIR.getDefaultState());
                        world.setBlockState(blockPos, Blocks.LAVA_CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
                    } else {
                        System.out.println("No water source found.");
                        BlockPos sourcePos2 = FluidTracker.findLavaFluidSource(world, blockPos);
                        if (sourcePos2 != null) {
                            world.playSound(null, blockPos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1f, 1f);
                            world.setBlockState(FluidTracker.findLavaFluidSource(world,blockPos.up()), Blocks.AIR.getDefaultState());
                            world.setBlockState(blockPos, Blocks.LAVA_CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
                        }else{
                            System.out.println("No water source found.");
                        }
                    }
                });
                scanThread.start();
            } else if (isWaterAndCauldronInteractionSimple(Blocks.CAULDRON, pos,posUp,posDown)) {
                world.playSound(null, blockPos, SoundEvents.BLOCK_WART_BLOCK_PLACE, SoundCategory.BLOCKS, 1f, 1f);
                world.setBlockState(blockPos, Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3,3), Block.NOTIFY_ALL);
            }
            //milk cauldron & honey cauldron
            else if (isLavaAndCauldronInteractionSimple(ModBlocks.CAULDRON_WITH_MILK, pos,posUp,posDown) || isLavaAndCauldronInteractionSimple(ModBlocks.CAULDRON_WITH_HONEY, pos,posUp,posDown)) {
                world.playSound(null, blockPos, SoundEvents.BLOCK_WART_BLOCK_PLACE, SoundCategory.BLOCKS, 1f, 1f);
                world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_EMBER.getDefaultState(), Block.NOTIFY_ALL);
                cauldronBlockCheck(world,blockPos);
            }
            //ember cauldron
            else if (isWaterAndCauldronInteractionSimple(ModBlocks.CAULDRON_WITH_EMBER, pos,posUp,posDown)) {
                world.playSound(null, blockPos, SoundEvents.BLOCK_WART_BLOCK_PLACE, SoundCategory.BLOCKS, 1f, 1f);
                world.setBlockState(blockPos, ModBlocks.CAULDRON_WITH_HALF_COBBLE_STONE.getDefaultState(), Block.NOTIFY_ALL);
                cauldronBlockCheck(world,blockPos);
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

    private static boolean isLavaAndDragonsBreathInteraction(BlockState pos, BlockState posUp, BlockState posDown) {
        return (pos.getBlock().equals(ModBlocks.CAULDRON_WITH_DRAGONS_BREATH) && isLavaBlock(posUp)) ||
                (posDown.getBlock().equals(ModBlocks.CAULDRON_WITH_DRAGONS_BREATH) && isLavaBlock(pos));
    }
    private static boolean isLavaAndCauldronInteractionSimple(Block target,BlockState pos, BlockState posUp, BlockState posDown) {
        return (pos.getBlock().equals(target) && isLavaBlock(posUp)) ||
                (posDown.getBlock().equals(target) && isLavaBlock(pos));
    }
    private static boolean isWaterAndCauldronInteractionSimple(Block target,BlockState pos, BlockState posUp, BlockState posDown) {
        return (pos.getBlock().equals(target) && isWaterBlock(posUp)) ||
                (posDown.getBlock().equals(target) && isWaterBlock(pos));
    }



    private static Integer getCauldronLevel(BlockState pos) {

        return pos.get(Properties.LEVEL_3);
    }

    private static boolean isWaterBlock(BlockState state) {
        return state.equals(Blocks.WATER.getDefaultState()) || state.getFluidState().isIn(FluidTags.WATER);
    }


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

package mypals.ml.dispenserInteractionManage;

import mypals.ml.CauldronBlockWatcher;
import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlocks;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldronBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.item.*;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

public class DispenserInteraction {
    public static void interact(ItemStack stack, BlockPointer pointer, CallbackInfoReturnable<ItemStack> cir){
        boolean blockIsDispenser = pointer.state().getBlock() == Blocks.DISPENSER;

        if (blockIsDispenser) {
            boolean itemIsBucket = stack.getItem() instanceof FluidModificationItem || stack.getItem() instanceof MilkBucketItem;
            boolean itemIsPotion = stack.getItem() instanceof PotionItem || stack.getItem() instanceof HoneyBottleItem || stack.getItem() instanceof OminousBottleItem || stack.getItem().equals(Items.DRAGON_BREATH);
            boolean itemIsBottle = stack.getItem() instanceof GlassBottleItem;
            boolean itemIsDye = stack.getItem() instanceof DyeItem || stack.getItem() instanceof GlowInkSacItem;
            if (itemIsBucket) {
                interactionWithBucket(stack, pointer, cir);
            }else if(itemIsPotion || itemIsBottle) {
                interactionWithPotion(stack, pointer, cir);
            } else if (itemIsDye) {
                interactionWithDye(stack,pointer,cir);
            }
        }
    }
    // 填充锅的方法
    private static void fillCauldronWithBucket(ItemStack stack, BlockPointer pointer, BlockPos targetPos, CallbackInfoReturnable<ItemStack> cir) {
        if (CauldronInteractionMap.BUCKET_TO_CAULDRON_MAP.containsKey(stack.getItem())) {
            Block cauldronBlockFilled = CauldronInteractionMap.BUCKET_TO_CAULDRON_MAP.get(stack.getItem());
            BlockState cauldronBlockFilledState = cauldronBlockFilled.getDefaultState();

            try {
                if(cauldronBlockFilled == Blocks.LAVA_CAULDRON) {
                    stack.decrement(1);
                    pointer.world().setBlockState(targetPos, cauldronBlockFilledState);
                    cir.setReturnValue(new ItemStack(Items.BUCKET));
                }else {
                    cauldronBlockFilledState = cauldronBlockFilledState.with(LeveledCauldronBlock.LEVEL, 3);
                    stack.decrement(1);
                    pointer.world().setBlockState(targetPos, cauldronBlockFilledState);
                    cir.setReturnValue(new ItemStack(Items.BUCKET));
                }
            } catch (Exception ignored) {
                CauldronFix.LOGGER.info(String.valueOf(ignored));
            }
            CauldronBlockWatcher.cauldronBlockCheck(pointer.world(),targetPos);
        }
    }
    private static void fillCauldronWithPotion(ItemStack stack, BlockPointer pointer, BlockPos targetPos, CallbackInfoReturnable<ItemStack> cir) {
        if (CauldronInteractionMap.POTION_TO_CAULDRON_MAP.containsKey(stack.getItem())) {
            Block cauldronBlockFilled = CauldronInteractionMap.POTION_TO_CAULDRON_MAP.get(stack.getItem());
            BlockState cauldronBlockFilledState = cauldronBlockFilled.getDefaultState();

            try {
                int level = 0;
                int light_level = 0;
                Block b = pointer.world().getBlockState(targetPos).getBlock();
                if(b.equals(ModBlocks.COLORED_CAULDRON) || b.equals(Blocks.WATER_CAULDRON) || b.equals(ModBlocks.HONEY_CAULDRON) || b.equals(ModBlocks.DRAGONS_BREATH_CAULDRON)|| b.equals(ModBlocks.BAD_OMEN_CAULDRON))
                {
                    level = pointer.world().getBlockState(targetPos).get(LeveledCauldronBlock.LEVEL);
                }
                if(level < 3) {
                    if((stack.getItem() == PotionContentsComponent.createStack(Items.POTION, Potions.WATER).getItem() && (
                                    b.equals(Blocks.WATER_CAULDRON) || b.equals(Blocks.CAULDRON) || b.equals(ModBlocks.COLORED_CAULDRON)
                            )) || (
                                    stack.getItem() == Items.DRAGON_BREATH && (
                                            b.equals(ModBlocks.DRAGONS_BREATH_CAULDRON) || b.equals(Blocks.CAULDRON)
                                    )) || (
                                            stack.getItem() == Items.HONEY_BOTTLE && (
                                            b.equals(ModBlocks.HONEY_CAULDRON) || b.equals(Blocks.CAULDRON)
                                            )) || (
                                                stack.getItem() == Items.OMINOUS_BOTTLE && (
                                                        b.equals(ModBlocks.BAD_OMEN_CAULDRON) || b.equals(Blocks.CAULDRON)
                                                ))
                    ) {
                        cauldronBlockFilledState = cauldronBlockFilledState.with(LeveledCauldronBlock.LEVEL, level + 1);
                        if(b.equals(ModBlocks.COLORED_CAULDRON))
                        {
                            light_level = pointer.world().getBlockState(targetPos).get(ColoredCauldron.LIGHT_LEVEL);
                            cauldronBlockFilledState = cauldronBlockFilledState.with(ColoredCauldron.LIGHT_LEVEL, light_level);
                        }
                        stack.decrement(1);
                        pointer.world().setBlockState(targetPos, cauldronBlockFilledState);

                        DispenserBlockEntity dispenser = pointer.blockEntity();
                        ItemStack emptyBottleStack = new ItemStack(Items.GLASS_BOTTLE);
                        if (stack.isEmpty()) {
                            // 如果当前堆栈已空，将空瓶子放在当前槽位
                            stack = emptyBottleStack;
                        } else {
                            // 如果堆栈未用完，尝试将空瓶子插入发射器的物品槽
                            if (!insertIntoDispenser(dispenser, emptyBottleStack)) {
                                // 如果发射器已满，将空瓶子掉落到世界
                                World world = pointer.world();
                                BlockPos pos = pointer.pos();
                                world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), emptyBottleStack));
                            }
                        }
                        cir.setReturnValue(stack);
                    }
                }
            } catch (Exception ignored) {
                CauldronFix.LOGGER.info(String.valueOf(ignored));
            }
            CauldronBlockWatcher.cauldronBlockCheck(pointer.world(),targetPos);
        }
    }

    private static void drainCauldronWithPotion(AbstractCauldronBlock cauldron, BlockState targetBlock, ItemStack stack, BlockPointer pointer, BlockPos targetPos, CallbackInfoReturnable<ItemStack> cir) {
        if (CauldronInteractionMap.CAULDRON_TO_POTION_MAP.containsKey(cauldron)) {
            Item filledPotionItem = CauldronInteractionMap.CAULDRON_TO_POTION_MAP.get(cauldron);

            BlockState cauldronBlockState = Blocks.WATER_CAULDRON.getDefaultState();
            if(targetBlock.getBlock().equals(ModBlocks.DRAGONS_BREATH_CAULDRON))
                cauldronBlockState = ModBlocks.DRAGONS_BREATH_CAULDRON.getDefaultState();
            else if(targetBlock.getBlock().equals(ModBlocks.HONEY_CAULDRON))
                cauldronBlockState = ModBlocks.HONEY_CAULDRON.getDefaultState();
            else if(targetBlock.getBlock().equals(ModBlocks.COLORED_CAULDRON))
                cauldronBlockState = ModBlocks.COLORED_CAULDRON.getDefaultState().with(ColoredCauldron.LIGHT_LEVEL,targetBlock.get(ColoredCauldron.LIGHT_LEVEL));

            int level = targetBlock.get(LeveledCauldronBlock.LEVEL);
            try {
                if (level > 1) {
                    cauldronBlockState = cauldronBlockState.with(LeveledCauldronBlock.LEVEL, level - 1);
                    stack.decrement(1);
                    pointer.world().setBlockState(targetPos, cauldronBlockState);
                    CauldronBlockWatcher.cauldronBlockCheck(pointer.world(), targetPos);

                    DispenserBlockEntity dispenser = (DispenserBlockEntity) pointer.blockEntity();
                    ItemStack filledBottleStack = new ItemStack(filledPotionItem);
                    if (stack.isEmpty()) {
                        // 如果当前堆栈已空，将空瓶子放在当前槽位
                        stack = filledBottleStack;
                    } else {
                        // 如果堆栈未用完，尝试将空瓶子插入发射器的物品槽
                        if (!insertIntoDispenser(dispenser, filledBottleStack)) {
                            // 如果发射器已满，将空瓶子掉落到世界
                            World world = pointer.world();
                            BlockPos pos = pointer.pos();
                            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), filledBottleStack));
                        }
                    }
                    // 返回当前物品堆栈
                    cir.setReturnValue(stack);
                }else if(level == 1)
                {
                    BlockState cauldronBlockEmptyState = Blocks.CAULDRON.getDefaultState();
                    stack.decrement(1);
                    pointer.world().setBlockState(targetPos, cauldronBlockEmptyState);
                    CauldronBlockWatcher.cauldronBlockCheck(pointer.world(), targetPos);
                    DispenserBlockEntity dispenser = (DispenserBlockEntity) pointer.blockEntity();
                    ItemStack filledBottleStack = new ItemStack(filledPotionItem);
                    if (stack.isEmpty()) {
                        stack = filledBottleStack;
                    } else {
                        // 如果堆栈未用完，尝试将空瓶子插入发射器的物品槽
                        if (!insertIntoDispenser(dispenser, filledBottleStack)) {
                            // 如果发射器已满，将空瓶子掉落到世界
                            World world = pointer.world();
                            BlockPos pos = pointer.pos();
                            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), filledBottleStack));
                        }
                    }
                    cir.setReturnValue(stack);
                }
            }catch (Exception ignored) {
                CauldronFix.LOGGER.info(String.valueOf(ignored));
            }

        }
    }
    private static boolean insertIntoDispenser(DispenserBlockEntity dispenser, ItemStack stack) {
        for (int i = 0; i < dispenser.size(); i++) {
            ItemStack currentStack = dispenser.getStack(i);
            if (currentStack.isEmpty()) {
                dispenser.setStack(i, stack.copy());
                return true;
            }
        }
        return false;
    }


    private static void drainCauldronWithBucket(AbstractCauldronBlock cauldron, BlockState targetBlock, ItemStack stack, BlockPointer pointer, BlockPos targetPos, CallbackInfoReturnable<ItemStack> cir) {
        if (CauldronInteractionMap.CAULDRON_TO_BUCKET_MAP.containsKey(cauldron) && cauldron.isFull(targetBlock)) {
            Item filledBucketItem = CauldronInteractionMap.CAULDRON_TO_BUCKET_MAP.get(cauldron);
            stack.decrement(1);
            pointer.world().setBlockState(targetPos, Blocks.CAULDRON.getDefaultState());
            CauldronBlockWatcher.cauldronBlockCheck(pointer.world(),targetPos);
            cir.setReturnValue(new ItemStack(filledBucketItem));

        }
    }

    private static void interactionWithPotion(ItemStack stack, BlockPointer pointer, CallbackInfoReturnable<ItemStack> cir){
        BlockPos targetPos = getTargetPos(pointer);
        BlockState targetBlock = pointer.world().getBlockState(targetPos);

        if (targetBlock.getBlock() instanceof AbstractCauldronBlock cauldron) {
            if (stack.getItem() == Items.POTION || stack.getItem() == Items.DRAGON_BREATH || stack.getItem() == Items.OMINOUS_BOTTLE || stack.getItem() == Items.HONEY_BOTTLE) {
                fillCauldronWithPotion(stack, pointer, targetPos, cir);
            } else if (cauldron == ModBlocks.COLORED_CAULDRON || cauldron == Blocks.WATER_CAULDRON || cauldron == ModBlocks.HONEY_CAULDRON || cauldron == ModBlocks.DRAGONS_BREATH_CAULDRON || cauldron == ModBlocks.BAD_OMEN_CAULDRON) {
                drainCauldronWithPotion((AbstractCauldronBlock)targetBlock.getBlock(), targetBlock, stack, pointer, targetPos, cir);
            }
        }
    }
    private static void interactionWithDye(ItemStack stack, BlockPointer pointer, CallbackInfoReturnable<ItemStack> cir){
        BlockPos targetPos = getTargetPos(pointer);
        BlockState targetBlock = pointer.world().getBlockState(targetPos);

        World world = pointer.world();
        if (targetBlock.getBlock() instanceof ColoredCauldron) {
            if (stack.getItem() instanceof DyeItem) {
                if (!world.isClient) {
                    BlockEntity blockEntity = world.getBlockEntity(targetPos);
                    if(blockEntity instanceof ColoredCauldronBlockEntity coloredCauldronBlockEntity)
                    {
                        coloredCauldronBlockEntity.setColor(((DyeItem) stack.getItem()).getColor());
                        coloredCauldronBlockEntity.toUpdatePacket();
                        world.playSound(null,targetPos, SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS,1,1);
                        stack.decrement(1);
                        world.updateListeners(targetPos, targetBlock, targetBlock, 0);
                        CauldronFix.rebuildBlock(targetPos);
                    }
                    cir.setReturnValue(stack);
                }
            }else if (stack.getItem() instanceof GlowInkSacItem) {
                if (!world.isClient) {
                    int target_light = targetBlock.get(ColoredCauldron.LIGHT_LEVEL);
                    if(target_light < 15)
                    {
                        target_light++;
                        world.setBlockState(targetPos,targetBlock.with(ColoredCauldron.LIGHT_LEVEL,target_light));
                        world.playSound(null,targetPos, SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS,1,1);
                        stack.decrement(1);
                        world.updateListeners(targetPos, targetBlock, targetBlock, 0);
                        CauldronFix.rebuildBlock(targetPos);
                    }
                    cir.setReturnValue(stack);

                }
            }
        }else if(stack.getItem() instanceof DyeItem dyeItem && targetBlock.getBlock() == Blocks.WATER_CAULDRON) {
            world.setBlockState(targetPos, ModBlocks.COLORED_CAULDRON.getDefaultState().with(Properties.LEVEL_3, targetBlock.get(Properties.LEVEL_3)));
            BlockEntity blockEntity = world.getBlockEntity (targetPos);
            if(blockEntity instanceof ColoredCauldronBlockEntity coloredCauldron) {
                coloredCauldron.resetColor();

                coloredCauldron.setColor(dyeItem.getColor());
                coloredCauldron.toUpdatePacket();
                CauldronFix.rebuildBlock(targetPos);
                world.playSound(null, targetPos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                stack.decrement(1);
                cir.setReturnValue(stack);
            }
        }
    }

    private static void interactionWithBucket(ItemStack stack, BlockPointer pointer, CallbackInfoReturnable<ItemStack> cir){
        BlockPos targetPos = getTargetPos(pointer);
        BlockState targetBlock = pointer.world().getBlockState(targetPos);

        if (targetBlock.getBlock() instanceof AbstractCauldronBlock cauldron) {
            if (stack.getItem() == Items.BUCKET) {
                drainCauldronWithBucket(cauldron, targetBlock, stack, pointer, targetPos, cir);
            } else if (cauldron == Blocks.CAULDRON) {
                fillCauldronWithBucket(stack, pointer, targetPos, cir);
            }
        }
    }

    private static BlockPos getTargetPos(BlockPointer pointer) {
        Direction dir = pointer.state().get(DispenserBlock.FACING);
        return pointer.blockEntity().getPos().offset(dir);
    }
}

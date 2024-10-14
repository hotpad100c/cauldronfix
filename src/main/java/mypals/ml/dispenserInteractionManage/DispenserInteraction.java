package mypals.ml.dispenserInteractionManage;

import mypals.ml.CauldronBlockWatcher;
import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class DispenserInteraction {
    public static void interact(ItemStack stack, BlockPointer pointer, CallbackInfoReturnable<ItemStack> cir){
        boolean blockIsDispenser = pointer.state().getBlock() == Blocks.DISPENSER;

        if (blockIsDispenser) {
            boolean itemIsBucket = stack.getItem() instanceof FluidModificationItem || stack.getItem() instanceof MilkBucketItem;
            boolean itemIsPotion = stack.getItem() instanceof PotionItem || stack.getItem() instanceof HoneyBottleItem || stack.getItem().equals(Items.DRAGON_BREATH);
            boolean itemIsBottle = stack.getItem() instanceof GlassBottleItem;

            if (itemIsBucket) {
                interactionWithBucket(stack, pointer, cir);
            }else if(itemIsPotion || itemIsBottle) {
                interactionWithPotion(stack, pointer, cir);
            }else{

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
                Block b = pointer.world().getBlockState(targetPos).getBlock();
                if(b.equals(Blocks.WATER_CAULDRON) || b.equals(ModBlocks.CAULDRON_WITH_HONEY) || b.equals(ModBlocks.CAULDRON_WITH_DRAGONS_BREATH))
                {
                    level = pointer.world().getBlockState(targetPos).get(LeveledCauldronBlock.LEVEL);
                }
                if(level < 3) {
                    if((stack.getItem() == PotionContentsComponent.createStack(Items.POTION, Potions.WATER).getItem() && (
                                    b.equals(Blocks.WATER_CAULDRON) || b.equals(Blocks.CAULDRON)
                            )) || (
                                    stack.getItem() == Items.DRAGON_BREATH && (
                                            b.equals(ModBlocks.CAULDRON_WITH_DRAGONS_BREATH) || b.equals(Blocks.CAULDRON)
                                    )) || (
                                            stack.getItem() == Items.HONEY_BOTTLE && (
                                            b.equals(ModBlocks.CAULDRON_WITH_HONEY) || b.equals(Blocks.CAULDRON)
                                            ))
                    ) {
                        cauldronBlockFilledState = cauldronBlockFilledState.with(LeveledCauldronBlock.LEVEL, level + 1);
                        stack.decrement(1);
                        pointer.world().setBlockState(targetPos, cauldronBlockFilledState);

                        DispenserBlockEntity dispenser = (DispenserBlockEntity) pointer.blockEntity();
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
            if(targetBlock.getBlock().equals(ModBlocks.CAULDRON_WITH_DRAGONS_BREATH))
                cauldronBlockState = ModBlocks.CAULDRON_WITH_DRAGONS_BREATH.getDefaultState();
            else if(targetBlock.getBlock().equals(ModBlocks.CAULDRON_WITH_HONEY))
                cauldronBlockState = ModBlocks.CAULDRON_WITH_HONEY.getDefaultState();

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
            if (stack.getItem() == Items.POTION || stack.getItem() == Items.DRAGON_BREATH || stack.getItem() == Items.HONEY_BOTTLE) {
                fillCauldronWithPotion(stack, pointer, targetPos, cir);
            } else if (cauldron == Blocks.WATER_CAULDRON || cauldron == ModBlocks.CAULDRON_WITH_HONEY || cauldron == ModBlocks.CAULDRON_WITH_DRAGONS_BREATH) {
                drainCauldronWithPotion((AbstractCauldronBlock)targetBlock.getBlock(), targetBlock, stack, pointer, targetPos, cir);
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

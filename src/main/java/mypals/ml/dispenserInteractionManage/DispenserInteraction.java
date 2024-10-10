package mypals.ml.dispenserInteractionManage;

import mypals.ml.CauldronBlockWatcher;
import mypals.ml.CauldronFix;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class DispenserInteraction {
    public static void interact(ItemStack stack, BlockPointer pointer, CallbackInfoReturnable<ItemStack> cir){
        boolean blockIsDispenser = pointer.state().getBlock() == Blocks.DISPENSER;

        if (blockIsDispenser) {
            boolean itemIsBucket = stack.getItem() instanceof FluidModificationItem;
            boolean itemIsPotion = stack.getItem() instanceof PotionItem;
            boolean itemIsBottle = stack.getItem() instanceof GlassBottleItem;

            if (itemIsBucket) {
                interactionWithBucket(stack, pointer, cir);
            }else if(itemIsPotion || itemIsBottle) {
                interactionWithPotion(stack, pointer, cir);
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
                boolean isWaterCauldron = pointer.world().getBlockState(targetPos).getBlock().equals(Blocks.WATER_CAULDRON);
                if(isWaterCauldron)
                {
                    level = pointer.world().getBlockState(targetPos).get(LeveledCauldronBlock.LEVEL);
                }
                if(level < 3) {
                    cauldronBlockFilledState = cauldronBlockFilledState.with(LeveledCauldronBlock.LEVEL, level + 1);
                    stack.decrement(1);
                    pointer.world().setBlockState(targetPos, cauldronBlockFilledState);
                    cir.setReturnValue(new ItemStack(Items.GLASS_BOTTLE));
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
            int level = targetBlock.get(LeveledCauldronBlock.LEVEL);
            try {
                if (level > 1) {
                    cauldronBlockState = cauldronBlockState.with(LeveledCauldronBlock.LEVEL, level - 1);
                    stack.decrement(1);
                    pointer.world().setBlockState(targetPos, cauldronBlockState);
                    CauldronBlockWatcher.cauldronBlockCheck(pointer.world(), targetPos);
                    cir.setReturnValue(new ItemStack(filledPotionItem));
                }else if(level == 1)
                {
                    BlockState cauldronBlockEmptyState = Blocks.CAULDRON.getDefaultState();
                    stack.decrement(1);
                    pointer.world().setBlockState(targetPos, cauldronBlockEmptyState);
                    CauldronBlockWatcher.cauldronBlockCheck(pointer.world(), targetPos);
                    cir.setReturnValue(new ItemStack(filledPotionItem));
                }
            }catch (Exception ignored) {
                CauldronFix.LOGGER.info(String.valueOf(ignored));
            }

        }
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
            if (stack.getItem() == Items.POTION) {
                fillCauldronWithPotion(stack, pointer, targetPos, cir);
            } else if (cauldron == Blocks.WATER_CAULDRON) {
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

package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.CauldronBlockWatcher;
import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlocks;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldronBlockEntity;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldron;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldronBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(AbstractCauldronBlock.class)
public abstract class AbstractCauldronBlockMixin {

    @Shadow
    protected abstract void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random);

    @Inject(at = @At("HEAD"), method = "onUseWithItem")
    private void mixinOnUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ItemActionResult> cir) {
        CauldronBlockWatcher.cauldronBlockCheckWithItem(world, pos, stack, player, hand, cir);
    }

    @WrapMethod(method = "onUseWithItem")
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, Operation<ItemActionResult> original) {

        ItemStack stack1 = isConcretePowder(stack);
        if (!stack1.isEmpty() && (state.getBlock().equals(Blocks.WATER_CAULDRON) || state.getBlock().equals(ModBlocks.MILK_CAULDRON))) {
            player.swingHand(hand, true);
            player.setStackInHand(hand, stack1);
            world.playSound(player, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS);
            return ItemActionResult.SUCCESS;
        } else if (stack.getItem() instanceof DyeItem && state.getBlock().equals(Blocks.WATER_CAULDRON)) {
            player.swingHand(hand, true);
            player.setStackInHand(hand, stack);
            world.setBlockState(pos, ModBlocks.COLORED_CAULDRON.getDefaultState().with(Properties.LEVEL_3, state.get(Properties.LEVEL_3)));
            //ColoredCauldron colorC = (ColoredCauldron) world.getBlockState(pos).getBlock();
            BlockEntity b = world.getBlockEntity(pos);
            if (b == null) {
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            } else if (b instanceof ColoredCauldronBlockEntity) {
                ((ColoredCauldronBlockEntity) b).setColor(((DyeItem) stack.getItem()).getColor());
                if (!player.isSpectator() && !player.isCreative())
                    stack.decrement(1);
            }
            world.playSound(player, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS);
            return ItemActionResult.SUCCESS;
        } else if (stack.getItem() instanceof PotionItem && (state.getBlock().equals(Blocks.WATER_CAULDRON) || state.getBlock().equals(Blocks.CAULDRON) || state.getBlock().equals(ModBlocks.COLORED_CAULDRON))) {

            PotionContentsComponent potionContentsComponent = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

            if (world.getBlockState(pos).getBlock().equals(Blocks.WATER_CAULDRON)) {
                    world.setBlockState(pos, ModBlocks.POTION_CAULDRON.getDefaultState().with(PotionCauldron.LEVEL, state.get(Properties.LEVEL_3)));
                    player.incrementStat(Stats.USE_CAULDRON);
                    if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity potionCauldron) {
                        potionCauldron.setColor(potionContentsComponent.getColor());

                        for (StatusEffectInstance effectInstance : potionContentsComponent.getEffects()) {
                            potionCauldron.addStatusEffect(effectInstance);
                            world.updateListeners(pos, state, state, 0);
                        }
                        CauldronFix.incrementFluidLevel(state, world, pos);
                        potionCauldron.toUpdatePacket();
                        player.swingHand(hand);
                        player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                        world.playSound(player, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1, 1);
                        world.updateListeners(pos, state, state, 0);
                        CauldronFix.rebuildBlock(pos);
                    }
                } else {
                    world.setBlockState(pos, ModBlocks.POTION_CAULDRON.getDefaultState());
                    player.incrementStat(Stats.USE_CAULDRON);
                    if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity potionCauldron) {
                        potionCauldron.setColor(potionContentsComponent.getColor());

                        for (StatusEffectInstance effectInstance : potionContentsComponent.getEffects()) {
                            potionCauldron.addStatusEffect(effectInstance);
                            world.updateListeners(pos, state, state, 0);
                        }
                        potionCauldron.toUpdatePacket();
                        player.swingHand(hand);
                        player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                        world.playSound(player, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1, 1);
                        world.updateListeners(pos, state, state, 0);
                        CauldronFix.rebuildBlock(pos);
                    }
                }

            }
        return original.call(stack, state, world, pos, player, hand, hit);
    }

    private static ItemStack isConcretePowder(ItemStack stack) {
        ItemStack concreteBlock = null;

        // Map the concrete powder to the corresponding concrete block
        if (stack.getItem() == Items.WHITE_CONCRETE_POWDER) {
            concreteBlock = Items.WHITE_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.ORANGE_CONCRETE_POWDER) {
            concreteBlock = Items.ORANGE_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.MAGENTA_CONCRETE_POWDER) {
            concreteBlock = Items.MAGENTA_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.LIGHT_BLUE_CONCRETE_POWDER) {
            concreteBlock = Items.LIGHT_BLUE_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.YELLOW_CONCRETE_POWDER) {
            concreteBlock = Items.YELLOW_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.LIME_CONCRETE_POWDER) {
            concreteBlock = Items.LIME_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.PINK_CONCRETE_POWDER) {
            concreteBlock = Items.PINK_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.GRAY_CONCRETE_POWDER) {
            concreteBlock = Items.GRAY_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.LIGHT_GRAY_CONCRETE_POWDER) {
            concreteBlock = Items.LIGHT_GRAY_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.CYAN_CONCRETE_POWDER) {
            concreteBlock = Items.CYAN_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.PURPLE_CONCRETE_POWDER) {
            concreteBlock = Items.PURPLE_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.BLUE_CONCRETE_POWDER) {
            concreteBlock = Items.BLUE_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.BROWN_CONCRETE_POWDER) {
            concreteBlock = Items.BROWN_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.GREEN_CONCRETE_POWDER) {
            concreteBlock = Items.GREEN_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.RED_CONCRETE_POWDER) {
            concreteBlock = Items.RED_CONCRETE.getDefaultStack();
        } else if (stack.getItem() == Items.BLACK_CONCRETE_POWDER) {
            concreteBlock = Items.BLACK_CONCRETE.getDefaultStack();
        }

        // If a matching concrete block is found, return it with the same count as the input stack
        if (concreteBlock != null) {
            return new ItemStack(concreteBlock.getRegistryEntry(), stack.getCount());
        }

        // If not a concrete powder, return an empty stack
        return ItemStack.EMPTY;
    }


}

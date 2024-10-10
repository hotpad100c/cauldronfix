package mypals.ml.block.advancedCauldron;

import mypals.ml.CauldronBlockWatcher;
import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface BehaciorMaps extends CauldronBehavior{
    Map<Item, CauldronBehavior> MILK_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("milk").map();
    Map<Item, CauldronBehavior> HONEY_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("honey").map();
    Map<Item, CauldronBehavior> DRAGON_BREATH_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("dragon_breath").map();
   static void registerBehaviorMaps(){
       EMPTY_CAULDRON_BEHAVIOR.map().put(Items.MILK_BUCKET, CauldronFix.createFillFromBucketBehavior(ModBlocks.CAULDRON_WITH_MILK, SoundEvents.ITEM_BUCKET_EMPTY));
        EMPTY_CAULDRON_BEHAVIOR.map().put(Items.HONEY_BOTTLE, CauldronFix.createFillFromBottleBehavior(ModBlocks.CAULDRON_WITH_HONEY,SoundEvents.ITEM_BOTTLE_EMPTY));
        EMPTY_CAULDRON_BEHAVIOR.map().put(Items.DRAGON_BREATH, CauldronFix.createFillFromBottleBehavior(ModBlocks.CAULDRON_WITH_DRAGONS_BREATH,SoundEvents.ITEM_BOTTLE_EMPTY));
        DRAGON_BREATH_CAULDRON_BEHAVIOR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
            if (!world.isClient) {
                Item item = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.DRAGON_BREATH)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                CauldronFix.decrementFluidLevel(state, world, pos);
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }

            return ItemActionResult.success(world.isClient);
        });
        DRAGON_BREATH_CAULDRON_BEHAVIOR.put(Items.DRAGON_BREATH, (state, world, pos, player, hand, stack) -> {
            if (CauldronFix.canIncrementFluidLevel(state)) {
                if (!world.isClient) {
                    Item item = stack.getItem();
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    player.incrementStat(Stats.USE_CAULDRON);
                    player.incrementStat(Stats.USED.getOrCreateStat(item));
                    CauldronFix.incrementFluidLevel(state, world, pos);
                    world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
                }

                return ItemActionResult.success(world.isClient);
            } else {
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
        });

       HONEY_CAULDRON_BEHAVIOR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
           if (!world.isClient) {
               Item item = stack.getItem();
               player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.HONEY_BOTTLE)));
               player.incrementStat(Stats.USE_CAULDRON);
               player.incrementStat(Stats.USED.getOrCreateStat(item));
               CauldronFix.decrementFluidLevel(state, world, pos);
               world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
               world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
           }

           return ItemActionResult.success(world.isClient);
       });
       HONEY_CAULDRON_BEHAVIOR.put(Items.HONEY_BOTTLE, (state, world, pos, player, hand, stack) -> {
           if (CauldronFix.canIncrementFluidLevel(state)) {
               if (!world.isClient) {
                   Item item = stack.getItem();
                   player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                   player.incrementStat(Stats.USE_CAULDRON);
                   player.incrementStat(Stats.USED.getOrCreateStat(item));
                   CauldronFix.incrementFluidLevel(state, world, pos);
                   world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                   world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
               }

               return ItemActionResult.success(world.isClient);
           } else {
               return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
           }
       });
       MILK_CAULDRON_BEHAVIOR.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> {
           if (!world.isClient) {
               Item item = stack.getItem();
               player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.MILK_BUCKET)));
               player.incrementStat(Stats.USE_CAULDRON);
               player.incrementStat(Stats.USED.getOrCreateStat(item));
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               CauldronBlockWatcher.cauldronBlockCheck(world,pos);
               world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
               world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
           }
           return ItemActionResult.success(world.isClient);
       });
    }

}

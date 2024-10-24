package mypals.ml.block.advancedCauldron;

import com.google.common.collect.Maps;
import mypals.ml.CauldronBlockWatcher;
import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlocks;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldronBlockEntity;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldronBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.predicate.item.PotionContentsPredicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.DyeColor;
import net.minecraft.util.ItemActionResult;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;

import java.util.Map;
import java.util.logging.Logger;

import static mypals.ml.CauldronFix.LOGGER;
import static mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron.LIGHT_LEVEL;

public interface BehaciorMaps extends CauldronBehavior{

    Map<Item, CauldronBehavior> COLORED_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("colored").map();

    Map<Item, CauldronBehavior> POTION_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("colored").map();
    Map<Item, CauldronBehavior> MILK_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("milk").map();
    Map<Item, CauldronBehavior> HONEY_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("honey").map();
    Map<Item, CauldronBehavior> DRAGON_BREATH_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("dragon_breath").map();
    Map<Item, CauldronBehavior> BAD_OMEN_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("bad_omen").map();
   static void registerBehaviorMaps(){

        EMPTY_CAULDRON_BEHAVIOR.map().put(Items.MILK_BUCKET, CauldronFix.createFillFromBucketBehavior(ModBlocks.MILK_CAULDRON, SoundEvents.ITEM_BUCKET_EMPTY));
        EMPTY_CAULDRON_BEHAVIOR.map().put(Items.HONEY_BOTTLE, CauldronFix.createFillFromBottleBehavior(ModBlocks.HONEY_CAULDRON,SoundEvents.ITEM_BOTTLE_EMPTY));
        EMPTY_CAULDRON_BEHAVIOR.map().put(Items.DRAGON_BREATH, CauldronFix.createFillFromBottleBehavior(ModBlocks.DRAGONS_BREATH_CAULDRON,SoundEvents.ITEM_BOTTLE_EMPTY));
        EMPTY_CAULDRON_BEHAVIOR.map().put(Items.OMINOUS_BOTTLE, CauldronFix.createFillFromBottleBehavior(ModBlocks.BAD_OMEN_CAULDRON,SoundEvents.ITEM_BOTTLE_EMPTY));


        for(Map.Entry<DyeColor, DyeItem> dye : DyeItem.DYES.entrySet())
        {
            //LOGGER.info("Registering:" + dye.getValue() + " to water cauldrons");
            WATER_CAULDRON_BEHAVIOR.map().put(dye.getValue(), CauldronFix.createDyeBehavior(ModBlocks.COLORED_CAULDRON,SoundEvents.ITEM_DYE_USE));
            //LOGGER.info("Registering:" + dye.getValue() + " to colored cauldrons");
            COLORED_CAULDRON_BEHAVIOR.put(dye.getValue(), (state, world,pos,player,hand,stack) ->{
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (stack.getItem() instanceof DyeItem && blockEntity instanceof ColoredCauldronBlockEntity colorCauldron && colorCauldron.getCauldronColor() != -1) {
                    if (!world.isClient) {
                        player.incrementStat(Stats.USE_CAULDRON);
                        colorCauldron.setColor(((DyeItem) stack.getItem()).getColor());
                        colorCauldron.toUpdatePacket();
                        player.swingHand(hand);
                        if(!player.isCreative() && !player.isSpectator())
                            stack.decrement(1);
                        world.playSound(player,pos,SoundEvents.ITEM_DYE_USE,SoundCategory.PLAYERS,1,1);

                        world.updateListeners(pos, state, state, 0);
                        CauldronFix.rebuildBlock(pos);
                    }
                    return ItemActionResult.success(world.isClient);
                }
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            });
        }

       assert MinecraftClient.getInstance().world != null;
       if(MinecraftClient.getInstance().world.getRegistryManager() != null){


           MinecraftClient.getInstance().world.getRegistryManager().getOptionalWrapper(RegistryKeys.POTION).ifPresent(registryWrapper -> {
                   addPotionToMaps(registryWrapper, Items.POTION);
                   addPotionToMaps(registryWrapper, Items.SPLASH_POTION);
                   addPotionToMaps(registryWrapper, Items.LINGERING_POTION);
               });
       }
       //for(Map.Entry<DyeColor, DyeItem> dye : DyeItem.DYES.entrySet())


       COLORED_CAULDRON_BEHAVIOR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
           if (!world.isClient) {
               Item item = stack.getItem();
               player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(PotionContentsComponent.createStack(Items.POTION, Potions.WATER).getItem())));
               player.incrementStat(Stats.USE_CAULDRON);
               player.incrementStat(Stats.USED.getOrCreateStat(item));
               CauldronFix.decrementFluidLevel(state, world, pos);
               world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
               world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
           }
           return ItemActionResult.success(world.isClient);
       });
       COLORED_CAULDRON_BEHAVIOR.put(PotionContentsComponent.createStack(Items.POTION, Potions.WATER).getItem(), (state, world, pos, player, hand, stack) -> {
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
       COLORED_CAULDRON_BEHAVIOR.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> {
           if (!world.isClient) {
               Item item = stack.getItem();
               player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.WATER_BUCKET)));
               player.incrementStat(Stats.USE_CAULDRON);
               player.incrementStat(Stats.USED.getOrCreateStat(item));
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               CauldronBlockWatcher.cauldronBlockCheck(world,pos);
               world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
               world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
           }
           return ItemActionResult.success(world.isClient);
       });
       COLORED_CAULDRON_BEHAVIOR.put(Items.GLOW_INK_SAC, (state, world, pos, player, hand, stack) -> {
           if (!world.isClient) {
               player.incrementStat(Stats.USE_CAULDRON);
               if(world.getBlockState(pos).get(LIGHT_LEVEL) < 15)
               {
                   world.setBlockState(pos,state.with(LIGHT_LEVEL,state.get(LIGHT_LEVEL) +1));
                   if(!player.isCreative() && !player.isSpectator())
                       stack.decrement(1);
                   player.swingHand(hand);
                   world.playSound(player,pos,SoundEvents.ITEM_DYE_USE,SoundCategory.PLAYERS,1,1);
               };
               world.updateListeners(pos, state, state, 0);
               CauldronFix.rebuildBlock(pos);
           }

           return ItemActionResult.success(world.isClient);
       });


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


       BAD_OMEN_CAULDRON_BEHAVIOR.put(Items.OMINOUS_BOTTLE, (state, world, pos, player, hand, stack) -> {
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
    private static void addPotionToMaps(
            RegistryWrapper<Potion> registryWrapper,
            Item item
    ) {
        // 使用 for-each 循环遍历所有药水条目
        for (RegistryEntry<Potion> potionEntry : registryWrapper.streamEntries().toList()) {
            Potion potion = potionEntry.value();

            ItemStack potionStack = PotionContentsComponent.createStack(item, potionEntry);
            LOGGER.info("Registering:" + potion + " to water cauldrons");
            WATER_CAULDRON_BEHAVIOR.map().put(potionStack.getItem(), CauldronFix.createDyeBehavior(ModBlocks.POTION_CAULDRON,SoundEvents.ITEM_BOTTLE_EMPTY));
            LOGGER.info("Registering:" + potion + " to potion cauldrons");
            POTION_CAULDRON_BEHAVIOR.put(potionStack.getItem(), (state, world,pos,player,hand, stack) ->{
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (stack.getItem() instanceof PotionItem && blockEntity instanceof PotionCauldronBlockEntity potionCauldron && potionCauldron.getCauldronColor() != -1) {
                    if (!world.isClient) {
                        player.incrementStat(Stats.USE_CAULDRON);


                        if(!(stack.getItem() instanceof PotionItem)){
                            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                        }
                        RegistryEntry<Potion> potionRegistryEntry = Registries.POTION.getEntry(potion);

                        potionCauldron.setColor(PotionContentsComponent.getColor(potionRegistryEntry));


                            PotionContentsComponent potionContentsComponent = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
                            potionContentsComponent.forEachEffect(potionCauldron::addStatusEffect);


                        potionCauldron.toUpdatePacket();
                        player.swingHand(hand);
                        if(!player.isCreative() && !player.isSpectator())
                            stack.decrement(1);
                        world.playSound(player,pos,SoundEvents.ITEM_BOTTLE_EMPTY,SoundCategory.PLAYERS,1,1);

                        world.updateListeners(pos, state, state, 0);
                        CauldronFix.rebuildBlock(pos);
                    }
                    return ItemActionResult.success(world.isClient);
                }
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            });
        }
    }


}

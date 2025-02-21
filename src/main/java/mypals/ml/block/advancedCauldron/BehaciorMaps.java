package mypals.ml.block.advancedCauldron;

import com.google.common.collect.Maps;
import mypals.ml.CauldronBlockWatcher;
import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlocks;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldronBlockEntity;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldron;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldronBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;

import java.text.Format;
import java.util.*;
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
                if (stack.getItem() instanceof DyeItem && blockEntity instanceof ColoredCauldronBlockEntity colorCauldron) {
                    if (!world.isClient) {
                        player.incrementStat(Stats.USE_CAULDRON);
                        colorCauldron.setColor(((DyeItem) stack.getItem()).getColor());
                        player.swingHand(hand);
                        player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                        if(!player.isCreative() && !player.isSpectator())
                            stack.decrement(1);
                        world.playSound(player,pos,SoundEvents.ITEM_DYE_USE,SoundCategory.PLAYERS,1,1);

                        world.updateListeners(pos, state, state, 0);
                        
                    }
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            });
            POTION_CAULDRON_BEHAVIOR.put(dye.getValue(), (state, world,pos,player,hand,stack) ->{
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (stack.getItem() instanceof DyeItem && blockEntity instanceof PotionCauldronBlockEntity potionCauldronBlockEntity) {
                    if (!world.isClient) {
                        player.incrementStat(Stats.USE_CAULDRON);
                        potionCauldronBlockEntity.setColor(((DyeItem) stack.getItem()).getColor());
                        player.swingHand(hand);
                        player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                        potionCauldronBlockEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON,100,1));
                        if(!player.isCreative() && !player.isSpectator())
                            stack.decrement(1);
                        world.playSound(player,pos,SoundEvents.ITEM_DYE_USE,SoundCategory.PLAYERS,1,1);

                        world.updateListeners(pos, state, state, 0);
                        
                    }
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            });
        }

       COLORED_CAULDRON_BEHAVIOR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
           if (!world.isClient) {
               Item item = stack.getItem();

               BlockEntity colorCauldron = world.getBlockEntity(pos);
               if(colorCauldron instanceof ColoredCauldronBlockEntity coloredCauldronBlockEntity) {

                   ItemStack potion = new ItemStack(Items.POTION);
                   ArrayList<StatusEffectInstance> effects = new ArrayList<>();
                   //ArrayList<String> effectNames = new ArrayList<>();
                   effects.add(new StatusEffectInstance(StatusEffects.POISON,100,1));
                   //effects.forEach(effect -> effectNames.add(effect.getTranslationKey()));
                   if(world.getBlockState(pos).get(LIGHT_LEVEL) > 0){
                       effects.add(new StatusEffectInstance(StatusEffects.GLOWING,world.getBlockState(pos).get(LIGHT_LEVEL)*100+500,1));
                   }
                   potion.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.of(Potions.WATER), Optional.of(coloredCauldronBlockEntity.getCauldronColor()),effects,Optional.empty()));


                   MutableText name = Text.translatable("item.cauldronfix.tinted_water_bottle").withColor(coloredCauldronBlockEntity.getCauldronColor());
                   potion.set(DataComponentTypes.ITEM_NAME, name);

                   NbtCompound nbtData = new NbtCompound();
                   nbtData.putBoolean("DyedPotion", true);
                   potion.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbtData));
                   player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, potion));
               }
               player.incrementStat(Stats.USE_CAULDRON);
               player.incrementStat(Stats.USED.getOrCreateStat(item));
               CauldronFix.decrementFluidLevel(state, world, pos);
               world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
               world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
           }
           return ActionResult.SUCCESS;
       });
       COLORED_CAULDRON_BEHAVIOR.put(PotionContentsComponent.createStack(Items.POTION, Potions.WATER).getItem(), (state, world, pos, player, hand, stack) -> {
           if (CauldronFix.canIncrementFluidLevel(state) && Objects.requireNonNull(stack.get(DataComponentTypes.POTION_CONTENTS)).matches(Potions.WATER)) {
               if (!world.isClient) {
                   Item item = stack.getItem();
                   player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                   player.incrementStat(Stats.USE_CAULDRON);
                   player.incrementStat(Stats.USED.getOrCreateStat(item));
                   CauldronFix.incrementFluidLevel(state, world, pos);
                   world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                   world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
               }

               return ActionResult.SUCCESS;
           } else {
               return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
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
           return ActionResult.SUCCESS;
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
               }
               world.updateListeners(pos, state, state, 0);
               
           }

           return ActionResult.SUCCESS;
       });

       POTION_CAULDRON_BEHAVIOR.put(Items.GLOW_INK_SAC, (state, world, pos, player, hand, stack) -> {
           if (!world.isClient) {
               player.incrementStat(Stats.USE_CAULDRON);
               if(world.getBlockState(pos).get(PotionCauldron.LIGHT_LEVEL) < 15)
               {
                   world.setBlockState(pos,state.with(PotionCauldron.LIGHT_LEVEL,state.get(PotionCauldron.LIGHT_LEVEL) +1));
                   if(!player.isCreative() && !player.isSpectator())
                       stack.decrement(1);
                   player.swingHand(hand);
                   world.playSound(player,pos,SoundEvents.ITEM_DYE_USE,SoundCategory.PLAYERS,1,1);
               };
               world.updateListeners(pos, state, state, 0);
               
           }

           return ActionResult.SUCCESS;
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
            return ActionResult.SUCCESS;
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

                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
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

           return ActionResult.SUCCESS;
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

               return ActionResult.SUCCESS;
           } else {
               return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
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

               return ActionResult.SUCCESS;
           } else {
               return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
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
           return ActionResult.SUCCESS;
       });
    }
}

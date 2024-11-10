package mypals.ml.block.advancedCauldron.coloredCauldrons;

import com.google.common.primitives.Ints;
import com.mojang.serialization.MapCodec;
import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlocks;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldron;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldronBlockEntity;
import mypals.ml.mixin.WolfEntityAccessor;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.function.ToIntFunction;
import java.util.random.RandomGenerator;

public class ColoredCauldron extends LeveledCauldronBlock implements BlockEntityProvider {
    public static final IntProperty LEVEL = Properties.LEVEL_3;
    public static final IntProperty LIGHT_LEVEL = IntProperty.of("light_level", 0, 15);
    public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE_1 = (state) -> (Integer) state.get(LIGHT_LEVEL);

    public ColoredCauldron(Biome.Precipitation precipitation, CauldronBehavior.CauldronBehaviorMap behaviorMap, Settings settings, Biome.Precipitation precipitation1) {
        super(precipitation, behaviorMap, settings);
        this.setDefaultState((this.stateManager.getDefaultState()).with(LEVEL, 1).with(LIGHT_LEVEL, 0));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof ColoredCauldron) {
            return new ColoredCauldronBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }


    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        this.spawnBreakParticles(world, player, pos, state);

        world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state));
        return state;
    }

    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {

    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (entity instanceof LivingEntity livingEntity && this.isEntityTouchingFluid(state, pos, livingEntity)) {
            ArrayList<ItemStack> equipments = new ArrayList<>();
            equipments.add(livingEntity.getEquippedStack(EquipmentSlot.HEAD));
            equipments.add(livingEntity.getEquippedStack(EquipmentSlot.CHEST));
            equipments.add(livingEntity.getEquippedStack(EquipmentSlot.BODY));
            equipments.add(livingEntity.getEquippedStack(EquipmentSlot.LEGS));
            equipments.add(livingEntity.getEquippedStack(EquipmentSlot.FEET));
            for (ItemStack equipment : equipments) {
                if (blockEntity instanceof ColoredCauldronBlockEntity colorCauldron && colorCauldron.getCauldronColor() != -1) {
                    if (!world.isClient) {
                        int colorNew = colorCauldron.getCauldronColor();
                        if (equipment.isIn(ItemTags.DYEABLE) && equipment.getOrDefault(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0, false)).rgb() != colorCauldron.getCauldronColor()) {

                            int colorOld = equipment.getOrDefault(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0, false)).rgb();
                            equipment.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(blendColors(colorOld, colorNew), true));
                            colorCauldron.decreaseColTime(world,pos,1);
                            world.updateListeners(pos, state, state, 0);
                        }
                    }
                }
            }

        }
    }
    public static int blendColors(int colorOld, int dyeColor) {
        int newRed = (dyeColor >> 16) & 0xFF;
        int newGreen = (dyeColor >> 8) & 0xFF;
        int newBlue = dyeColor & 0xFF;

        int oldRed = (colorOld >> 16) & 0xFF;
        int oldGreen = (colorOld >> 8) & 0xFF;
        int oldBlue = colorOld & 0xFF;

        var newColor = new int[3];
        newColor[0] = newRed;
        newColor[1] = newGreen;
        newColor[2] = newBlue;

        var oldColor = new int[3];
        oldColor[0] = oldRed;
        oldColor[1] = oldGreen;
        oldColor[2] = oldBlue;
        if (!Arrays.equals(oldColor, new int[]{-1, -1, -1})) {
            var avgColor = new int[3];
            avgColor[0] = (oldColor[0] + newColor[0]) / 50;
            avgColor[1] = (oldColor[1] + newColor[1]) / 50;
            avgColor[2] = (oldColor[2] + newColor[2]) / 50;

            var avgMax = (Ints.max(oldColor) + Ints.max(newColor)) / 2.0f;

            var maxOfAvg = (float) Ints.max(avgColor);
            var gainFactor = (avgMax / maxOfAvg);

            oldColor[0] = (int) (avgColor[0] * gainFactor);
            oldColor[1] = (int) (avgColor[1] * gainFactor);
            oldColor[2] = (int) (avgColor[2] * gainFactor);
        } else {
            oldColor = newColor;
        }

        return (oldColor[0] << 16) | (oldColor[1] << 8) | oldColor[2];
    }


    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        assert blockEntity != null;
        blockEntity.toUpdatePacket();

        NbtCompound not_a_potion = new NbtCompound();
        not_a_potion.putBoolean("DyedPotion", true);
        //if (stack.isIn(ItemTags.DYEABLE) && blockEntity instanceof ColoredCauldronBlockEntity colorCauldron && colorCauldron.getCauldronColor() != -1) {
        if (stack.isIn(ItemTags.DYEABLE) && blockEntity instanceof ColoredCauldronBlockEntity colorCauldron && colorCauldron.getCauldronColor() != -1) {

            if (!world.isClient) {
                player.incrementStat(Stats.USE_CAULDRON);
                stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(colorCauldron.getCauldronColor(), true));
                LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
                player.swingHand(hand);
                world.playSound(player, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS, 1, 1);

                world.updateListeners(pos, state, state, 0);
            }

            return ItemActionResult.success(world.isClient);
        } else if (stack.getItem() instanceof PotionItem && !Objects.requireNonNull(stack.get(DataComponentTypes.POTION_CONTENTS)).matches(Potions.WATER)) {
            PotionContentsComponent potionContentsComponent = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

            if (blockEntity instanceof ColoredCauldronBlockEntity coloredCauldronBlockEntity) {
                int cauldronColor = coloredCauldronBlockEntity.getCauldronColor();

                if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).matches(not_a_potion)){
                    coloredCauldronBlockEntity.setColor(potionContentsComponent.getColor());
                    if (CauldronFix.canIncrementFluidLevel(state)) {
                        CauldronFix.incrementFluidLevel(world.getBlockState(pos), world, pos);
                    }
                } else {
                    world.setBlockState(pos, ModBlocks.POTION_CAULDRON.getDefaultState().with(PotionCauldron.LIGHT_LEVEL, state.get(ColoredCauldron.LIGHT_LEVEL)).with(PotionCauldron.LEVEL, state.get(ColoredCauldron.LEVEL)));
                    if (world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity potionCauldron) {
                        player.incrementStat(Stats.USE_CAULDRON);
                        potionCauldron.setColor(potionContentsComponent.getColor());
                        potionCauldron.setColor(cauldronColor);
                        if (CauldronFix.canIncrementFluidLevel(state) && world.getBlockState(pos).getBlock() instanceof PotionCauldron) {
                            CauldronFix.incrementFluidLevel(world.getBlockState(pos), world, pos);
                            for (StatusEffectInstance effectInstance : potionContentsComponent.getEffects()) {
                                potionCauldron.addStatusEffect(effectInstance);
                            }
                            world.updateListeners(pos, state, state, 0);
                        }
                    }
                }

                player.swingHand(hand);
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                world.playSound(player, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1, 1);
                world.updateListeners(pos, state, state, 0);
            }
        }
        CauldronBehavior cauldronBehavior = this.behaviorMap.map().get(stack.getItem());
        return cauldronBehavior.interact(state, world, pos, player, hand, stack);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, LIGHT_LEVEL);
    }

}

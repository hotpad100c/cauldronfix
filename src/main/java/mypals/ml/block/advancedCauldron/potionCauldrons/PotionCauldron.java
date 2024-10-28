package mypals.ml.block.advancedCauldron.potionCauldrons;

import mypals.ml.CauldronFix;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldronBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.predicate.item.PotionContentsPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import static mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron.LIGHT_LEVEL;

public class PotionCauldron extends LeveledCauldronBlock implements BlockEntityProvider {
    public static final IntProperty LEVEL = Properties.LEVEL_3;
    public static final IntProperty LIGHT_LEVEL = IntProperty.of("light_level", 0, 15);
    public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE_2 = (state) -> (Integer) state.get(LIGHT_LEVEL);

    public PotionCauldron(Biome.Precipitation precipitation, CauldronBehavior.CauldronBehaviorMap behaviorMap, Settings settings, Biome.Precipitation precipitation1) {
        super(precipitation, behaviorMap, settings);
        this.setDefaultState((this.stateManager.getDefaultState()).with(LEVEL, 1).with(LIGHT_LEVEL, 0));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof PotionCauldron) {
            return new PotionCauldronBlockEntity(pos, state);
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
        /*BlockEntity blockEntity = world.getBlockEntity(pos);
        if(entity instanceof LivingEntity livingEntity && this.isEntityTouchingFluid(state, pos, livingEntity)){

        }*/
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if ((stack.getItem() instanceof PotionItem)) {
            PotionContentsComponent potionContentsComponent = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PotionCauldronBlockEntity potionCauldron) {
                if (!world.isClient) {
                    player.incrementStat(Stats.USE_CAULDRON);
                    potionCauldron.setColor(potionContentsComponent.getColor());
                    if(CauldronFix.canIncrementFluidLevel(state))
                    {
                        for(StatusEffectInstance effectInstance : potionContentsComponent.getEffects()){
                            potionCauldron.addStatusEffect(effectInstance);
                            world.updateListeners(pos, state, state, 0);
                        }
                        CauldronFix.incrementFluidLevel(state,world,pos);
                    }else{
                        Iterable<StatusEffectInstance> itemEffects = potionContentsComponent.getEffects();
                        Map<RegistryEntry<StatusEffect>, StatusEffectInstance> cauldronEffects = potionCauldron.getStatusEffect();
                        for (StatusEffectInstance itemEffect : itemEffects) {

                            RegistryEntry<StatusEffect> effectType = itemEffect.getEffectType();


                            for (RegistryEntry<StatusEffect> cauldronKey : cauldronEffects.keySet()) {
                                if (cauldronKey.equals(effectType)) {

                                    StatusEffectInstance cauldronEffect = cauldronEffects.get(cauldronKey);


                                    int newDuration = cauldronEffect.getDuration() + itemEffect.getDuration() / 6;
                                    cauldronEffects.put(cauldronKey, new StatusEffectInstance(effectType, newDuration, cauldronEffect.getAmplifier()));

                                }else{
                                    potionCauldron.addStatusEffect(itemEffect);
                                }
                            }
                        }
                    }
                    potionCauldron.toUpdatePacket();
                    player.swingHand(hand);
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    world.playSound(player, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1, 1);
                    world.updateListeners(pos, state, state, 0);
                    CauldronFix.rebuildBlock(pos);
                }
                return ItemActionResult.success(world.isClient);
            }
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }else if (stack.getItem() instanceof GlassBottleItem || stack.getItem() instanceof ArrowItem ) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PotionCauldronBlockEntity potionCauldron) {
                if (!world.isClient) {

                    Map<RegistryEntry<StatusEffect>, StatusEffectInstance> cauldronEffects = potionCauldron.getStatusEffect();

                    PotionContentsComponent potionContentsComponent = PotionContentsComponent.DEFAULT;

                    List<StatusEffectInstance> effectInstances = new ArrayList<>(cauldronEffects.values());

                    if (!PotionContentsComponent.mixColors(effectInstances).isEmpty()) {
                        int lightLevel = world.getBlockState(pos).get(PotionCauldron.LIGHT_LEVEL);
                        if (lightLevel > 0) {
                            effectInstances.add(new StatusEffectInstance(StatusEffects.GLOWING, lightLevel * 100, 1));
                        }

                        potionContentsComponent = new PotionContentsComponent(
                                Optional.empty(),
                                Optional.of(PotionContentsComponent.mixColors(effectInstances).getAsInt()),
                                effectInstances
                        );
                    }

                    if(stack.getItem() instanceof GlassBottleItem ) {
                        ItemStack potionStack = new ItemStack(Items.POTION);
                        Objects.requireNonNull(potionStack.set(DataComponentTypes.POTION_CONTENTS, potionContentsComponent));
                        player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, potionStack));
                    }else if (stack.getItem() instanceof ArrowItem){
                        ItemStack arrowStack = new ItemStack(Items.TIPPED_ARROW);
                        arrowStack.setCount(stack.getCount());
                        Objects.requireNonNull(arrowStack.set(DataComponentTypes.POTION_CONTENTS, potionContentsComponent));
                        player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, arrowStack));
                    }

                    player.incrementStat(Stats.USE_CAULDRON);
                    potionCauldron.toUpdatePacket();
                    player.swingHand(hand);

                    CauldronFix.decrementFluidLevel(state, world, pos);
                    world.playSound(player, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.PLAYERS, 1, 1);
                    world.updateListeners(pos, state, state, 0);
                    CauldronFix.rebuildBlock(pos);
                }
                return ItemActionResult.success(world.isClient);
            }
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        CauldronBehavior cauldronBehavior = this.behaviorMap.map().get(stack.getItem());
        return cauldronBehavior.interact(state, world, pos, player, hand, stack);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, LIGHT_LEVEL);
    }


}

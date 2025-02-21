package mypals.ml.block.advancedCauldron.potionCauldrons;

import mypals.ml.CauldronFix;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.ToIntFunction;

public class PotionCauldron extends LeveledCauldronBlock implements BlockEntityProvider {
    public static final IntProperty LEVEL = Properties.LEVEL_3;
    public static final IntProperty LIGHT_LEVEL = IntProperty.of("light_level", 0, 15);
    public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE_2 = state -> (Integer) state.get(LIGHT_LEVEL);

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
        if(world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity potionCauldronBlockEntity && !world.isClient() && !player.isCreative()) {
            AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(world, pos.toBottomCenterPos().getX(), pos.toBottomCenterPos().getY(), pos.toBottomCenterPos().getZ());
            areaEffectCloudEntity.setDuration(100);
            areaEffectCloudEntity.setRadius(1);
            areaEffectCloudEntity.setRadiusGrowth((7.0f - areaEffectCloudEntity.getRadius()) / (float) areaEffectCloudEntity.getDuration());
            Map<RegistryEntry<StatusEffect>, StatusEffectInstance> cauldronEffects = potionCauldronBlockEntity.getStatusEffect();
            for(StatusEffectInstance statusEffectInstance : cauldronEffects.values()) {
                areaEffectCloudEntity.addEffect(statusEffectInstance);
            }
            world.spawnEntity(areaEffectCloudEntity);
        }
        world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state));
        return state;
    }
    @Override
    public void onDestroyedByExplosion(ServerWorld world, BlockPos pos, Explosion explosion) {
        if(world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity potionCauldronBlockEntity && !world.isClient()) {
            if(explosion.getCausingEntity() instanceof PlayerEntity player && player.isCreative()){return;}
            AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(world, pos.toBottomCenterPos().getX(), pos.toBottomCenterPos().getY(), pos.toBottomCenterPos().getZ());
            areaEffectCloudEntity.setDuration(100);
            areaEffectCloudEntity.setRadius(1);
            areaEffectCloudEntity.setRadiusGrowth((7.0f - areaEffectCloudEntity.getRadius()) / (float) areaEffectCloudEntity.getDuration());
            Map<RegistryEntry<StatusEffect>, StatusEffectInstance> cauldronEffects = potionCauldronBlockEntity.getStatusEffect();
            for(StatusEffectInstance statusEffectInstance : cauldronEffects.values()) {
                areaEffectCloudEntity.addEffect(statusEffectInstance);
            }
            world.spawnEntity(areaEffectCloudEntity);
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity livingEntity && world.getBlockEntity(pos) instanceof PotionCauldronBlockEntity potionCauldronBlockEntity) {
            Map<RegistryEntry<StatusEffect>, StatusEffectInstance> cauldronEffects = potionCauldronBlockEntity.getStatusEffect();
            for(StatusEffectInstance statusEffectInstance : cauldronEffects.values()) {
                StatusEffectInstance effectInstance = new StatusEffectInstance(statusEffectInstance.getEffectType(),
                        1,
                        statusEffectInstance.getAmplifier(),
                        statusEffectInstance.shouldShowParticles(),
                        statusEffectInstance.shouldShowIcon());
                livingEntity.addStatusEffect(effectInstance);
            }
            potionCauldronBlockEntity.decreaseColTime(world,pos,1);
        }
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if ((stack.getItem() instanceof PotionItem)) {
            PotionContentsComponent potionContentsComponent = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PotionCauldronBlockEntity potionCauldron) {
                if (!world.isClient) {
                    player.incrementStat(Stats.USE_CAULDRON);
                    potionCauldron.setColor(potionContentsComponent.getColor());
                    if(potionContentsComponent.matches(Potions.WATER)) {
                        Map<RegistryEntry<StatusEffect>, StatusEffectInstance> cauldronEffects = potionCauldron.getStatusEffect();
                        for (RegistryEntry<StatusEffect> cauldronKey : cauldronEffects.keySet()) {
                            StatusEffectInstance cauldronEffect = cauldronEffects.get(cauldronKey);
                            RegistryEntry<StatusEffect> effectType = cauldronEffect.getEffectType();
                            int newDuration = cauldronEffect.getDuration() / 6 ;
                            cauldronEffects.put(cauldronKey, new StatusEffectInstance(effectType, newDuration, cauldronEffect.getAmplifier()));
                        }
                    }
                    if (CauldronFix.canIncrementFluidLevel(state)) {
                        for (StatusEffectInstance effectInstance : potionContentsComponent.getEffects()) {
                            potionCauldron.addStatusEffect(effectInstance);
                            world.updateListeners(pos, state, state, 0);
                        }
                        CauldronFix.incrementFluidLevel(state, world, pos);
                    } else {
                        Iterable<StatusEffectInstance> itemEffects = potionContentsComponent.getEffects();
                        Map<RegistryEntry<StatusEffect>, StatusEffectInstance> cauldronEffects = potionCauldron.getStatusEffect();
                        for (StatusEffectInstance itemEffect : itemEffects) {

                            RegistryEntry<StatusEffect> effectType = itemEffect.getEffectType();


                            for (RegistryEntry<StatusEffect> cauldronKey : cauldronEffects.keySet()) {
                                if (cauldronKey.equals(effectType)) {

                                    StatusEffectInstance cauldronEffect = cauldronEffects.get(cauldronKey);


                                    int newDuration = cauldronEffect.getDuration() + itemEffect.getDuration() / 6;
                                    cauldronEffects.put(cauldronKey, new StatusEffectInstance(effectType, newDuration, cauldronEffect.getAmplifier()));

                                } else {
                                    potionCauldron.addStatusEffect(itemEffect);
                                }
                            }
                        }
                    }
                    potionCauldron.setCollideTime(PotionCauldronBlockEntity.DEFAULT_COLLIDE_TIME);
                    potionCauldron.toUpdatePacket();
                    player.swingHand(hand);
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    world.playSound(player, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS, 1, 1);
                    world.updateListeners(pos, state, state, 0);
                    
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        } else if (stack.getItem() instanceof GlassBottleItem || stack.getItem() instanceof ArrowItem) {
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
                                Optional.of(potionCauldron.getCauldronColor()),
                                effectInstances,
                                Optional.empty()
                        );
                    }

                    if (stack.getItem() instanceof GlassBottleItem) {
                        ItemStack potionStack = new ItemStack(Items.POTION);
                        Objects.requireNonNull(potionStack.set(DataComponentTypes.POTION_CONTENTS, potionContentsComponent));
                        MutableText name = Text.translatable("item.cauldronfix.potion").withColor(potionCauldron.getCauldronColor());
                        potionStack.set(DataComponentTypes.ITEM_NAME, name);
                        player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, potionStack));
                    } else if (stack.getItem() instanceof ArrowItem) {
                        ItemStack arrowStack = new ItemStack(Items.TIPPED_ARROW);
                        arrowStack.setCount(stack.getCount());
                        Objects.requireNonNull(arrowStack.set(DataComponentTypes.POTION_CONTENTS, potionContentsComponent));
                        MutableText name = Text.translatable("item.cauldronfix.tipped_arrow").withColor(potionCauldron.getCauldronColor());
                        arrowStack.set(DataComponentTypes.ITEM_NAME, name);
                        player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, arrowStack));
                        stack.decrementUnlessCreative(stack.getCount(),player);
                    }


                    player.incrementStat(Stats.USE_CAULDRON);
                    potionCauldron.toUpdatePacket();
                    player.swingHand(hand);

                    CauldronFix.decrementFluidLevel(state, world, pos);
                    world.playSound(player, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.PLAYERS, 1, 1);
                    world.updateListeners(pos, state, state, 0);
                    
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        CauldronBehavior cauldronBehavior = this.behaviorMap.map().get(stack.getItem());
        return cauldronBehavior.interact(state, world, pos, player, hand, stack);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, LIGHT_LEVEL);
    }


}

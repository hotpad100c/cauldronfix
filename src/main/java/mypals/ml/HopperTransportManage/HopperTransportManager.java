package mypals.ml.HopperTransportManage;

import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlocks;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldronBlockEntity;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldron;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldronBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron.LIGHT_LEVEL;

public class HopperTransportManager {
    /**
     * Manages the transfer of fluids between cauldrons using hoppers.
     * This method handles various types of cauldrons and their interactions,
     * including water, colored, honey, bad omen, and other custom cauldrons.
     *
     * @param cauldronTransferCooldown The cooldown time for cauldron transfers.
     * @param world                    The world in which the transfer is taking place.
     * @param pos                      The position of the hopper block.
     * @param state                    The current state of the hopper block.
     * @param blockEntity              The hopper block entity.
     * @param ci                       Callback info for the method injection (unused in this method).
     * @return An integer representing the cooldown time for the next transfer attempt.
     * Returns 0 if no transfer occurred, 8 or 16 depending on the type of transfer.
     */
    public static int hopperTransfer(Integer cauldronTransferCooldown, World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {


        if (world.isReceivingRedstonePower(pos)) {
            return 0;
        }
        BlockPos abovePos = pos.up();
        BlockState aboveBlockState = world.getBlockState(abovePos);

        if (!(aboveBlockState.getBlock() == Blocks.WATER_CAULDRON) &&
                !(aboveBlockState.getBlock() == Blocks.LAVA_CAULDRON) &&
                !(aboveBlockState.getBlock() == ModBlocks.HONEY_CAULDRON) &&
                !(aboveBlockState.getBlock() == ModBlocks.MILK_CAULDRON) &&
                !(aboveBlockState.getBlock() == ModBlocks.DRAGONS_BREATH_CAULDRON) &&
                !(aboveBlockState.getBlock() == ModBlocks.COLORED_CAULDRON) &&
                !(aboveBlockState.getBlock() == ModBlocks.POTION_CAULDRON) &&
                !(aboveBlockState.getBlock() == ModBlocks.BAD_OMEN_CAULDRON)) {
            return 0;
        }

        BlockPos downPos = pos.offset(state.get(HopperBlock.FACING));
        BlockState downBlockState = world.getBlockState(downPos);

        if (downBlockState.getBlock() instanceof HopperBlock) {
            downPos = isPointingToAnotherHopper(world, downPos, downBlockState, 8, 8, getParticle(world.getBlockState(abovePos).getBlock()));
            downBlockState = world.getBlockState(downPos);
        }

        if (aboveBlockState.getBlock() == Blocks.WATER_CAULDRON) {

            if (cauldronTransferCooldown <= 0 && (downBlockState.getBlock() == Blocks.WATER_CAULDRON || downBlockState.getBlock() == Blocks.CAULDRON)) {


                if (downBlockState.getBlock() != Blocks.CAULDRON) {
                    int downWaterLevel = downBlockState.get(Properties.LEVEL_3);
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);

                    if (downWaterLevel < 3 && aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        CauldronFix.incrementFluidLevel(downBlockState, world, downPos, false, 1);
                    }
                    world.markDirty(pos);
                    return 8;
                } else {
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);
                    if (aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        world.setBlockState(downPos, Blocks.WATER_CAULDRON.getDefaultState());
                        return 8;
                    }
                }

            }
        } else if (world.getBlockEntity(abovePos) instanceof ColoredCauldronBlockEntity coloredCauldronBlockAbove) {
            if (cauldronTransferCooldown <= 0 && (downBlockState.getBlock() == ModBlocks.COLORED_CAULDRON || downBlockState.getBlock() == Blocks.CAULDRON || downBlockState.getBlock() == Blocks.WATER_CAULDRON || downBlockState.getBlock() == ModBlocks.POTION_CAULDRON)) {


                if (downBlockState.getBlock() != Blocks.CAULDRON) {
                    int downWaterLevel = downBlockState.get(Properties.LEVEL_3);
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);


                    if (downWaterLevel < 3 && aboveWaterLevel > 0) {
                        int color = coloredCauldronBlockAbove.getCauldronColor();

                        if (downBlockState.getBlock() == ModBlocks.COLORED_CAULDRON) {
                            downBlockState.with(ColoredCauldron.LIGHT_LEVEL, aboveBlockState.get(ColoredCauldron.LIGHT_LEVEL));
                            ColoredCauldronBlockEntity coloredCauldronBlockDown = (ColoredCauldronBlockEntity) world.getBlockEntity(downPos);
                            if (coloredCauldronBlockDown == null) return 16;
                            coloredCauldronBlockDown.setColor(color);
                        }
                        else if (downBlockState.getBlock() == ModBlocks.POTION_CAULDRON) {
                            downBlockState.with(PotionCauldron.LIGHT_LEVEL, aboveBlockState.get(ColoredCauldron.LIGHT_LEVEL));

                            if(CauldronFix.canIncrementFluidLevel(downBlockState)){
                                CauldronFix.incrementFluidLevel(downBlockState,world,downPos);
                            }
                            ArrayList<StatusEffectInstance> effects = new ArrayList<>();
                            effects.add(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
                            if (world.getBlockState(abovePos).get(ColoredCauldron.LIGHT_LEVEL) > 0) {
                                effects.add(new StatusEffectInstance(StatusEffects.GLOWING, world.getBlockState(abovePos).get(ColoredCauldron.LIGHT_LEVEL) * 100 + 500, 1));
                            }

                            PotionCauldronBlockEntity potionCauldronBlockEntity = (PotionCauldronBlockEntity) world.getBlockEntity(downPos);
                            if (potionCauldronBlockEntity == null) return 16;
                            potionCauldronBlockEntity.setColor(color);
                            for (StatusEffectInstance statusEffectInstance : effects)
                                potionCauldronBlockEntity.addStatusEffect(statusEffectInstance);
                        } else if(downBlockState.getBlock() == Blocks.WATER_CAULDRON){

                            world.setBlockState(downPos, ModBlocks.COLORED_CAULDRON.getDefaultState().with(Properties.LEVEL_3, aboveBlockState.get(ColoredCauldron.LEVEL)).with(ColoredCauldron.LIGHT_LEVEL, aboveBlockState.get(ColoredCauldron.LIGHT_LEVEL)));
                            CauldronFix.incrementFluidLevel(world.getBlockState(downPos), world, downPos, false, 1);
                            ColoredCauldronBlockEntity coloredCauldronBlockDown = (ColoredCauldronBlockEntity) world.getBlockEntity(downPos);
                            if (coloredCauldronBlockDown == null) return 16;
                            coloredCauldronBlockDown.setColor(color);
                        }
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);

                    }
                    world.markDirty(pos);
                    return 16;
                } else {
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);
                    if (aboveWaterLevel > 0) {

                        int color = coloredCauldronBlockAbove.getCauldronColor();

                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        world.setBlockState(downPos, ModBlocks.COLORED_CAULDRON.getDefaultState().with(ColoredCauldron.LIGHT_LEVEL, aboveBlockState.get(ColoredCauldron.LIGHT_LEVEL)));

                        ColoredCauldronBlockEntity coloredCauldronBlockDown = (ColoredCauldronBlockEntity) world.getBlockEntity(downPos);
                        if (coloredCauldronBlockDown == null) return 16;
                        if (color != coloredCauldronBlockDown.getCauldronColor()) {
                            coloredCauldronBlockDown.setColor(color);
                        }

                        return 16;
                    }
                }

            }
        } else if (world.getBlockEntity(abovePos) instanceof PotionCauldronBlockEntity potionCauldronBlockEntityAbove) {
            if (cauldronTransferCooldown <= 0 && (downBlockState.getBlock() == ModBlocks.COLORED_CAULDRON || downBlockState.getBlock() == Blocks.CAULDRON || downBlockState.getBlock() == Blocks.WATER_CAULDRON || downBlockState.getBlock() == ModBlocks.POTION_CAULDRON)) {


                Map<RegistryEntry<StatusEffect>, StatusEffectInstance> cauldronEffects = potionCauldronBlockEntityAbove.getStatusEffect();
                ArrayList<StatusEffectInstance> statusEffectInstanceList = new ArrayList<>(cauldronEffects.values());
                int cauldronColorAbove = potionCauldronBlockEntityAbove.getCauldronColor();

                if (downBlockState.getBlock() != Blocks.CAULDRON) {

                    int downWaterLevel = downBlockState.get(Properties.LEVEL_3);
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);
                    if (downWaterLevel < 3 && aboveWaterLevel > 0) {
                        if (downBlockState.getBlock().equals(ModBlocks.COLORED_CAULDRON)) {
                            ColoredCauldronBlockEntity coloredCauldronBlockEntityDown = (ColoredCauldronBlockEntity) world.getBlockEntity(downPos);
                            int cauldronColorDown = coloredCauldronBlockEntityDown.getCauldronColor();

                            world.setBlockState(downPos, ModBlocks.POTION_CAULDRON.getDefaultState().with(PotionCauldron.LIGHT_LEVEL, aboveBlockState.get(PotionCauldron.LIGHT_LEVEL)).with(PotionCauldron.LEVEL, downBlockState.get(ColoredCauldron.LEVEL)));
                            if(CauldronFix.canIncrementFluidLevel(world.getBlockState(downPos))){
                                CauldronFix.incrementFluidLevel(world.getBlockState(downPos), world, downPos, false, 1);}

                            if (world.getBlockEntity(downPos) instanceof PotionCauldronBlockEntity newPotionCauldron) {
                                newPotionCauldron.setColor(cauldronColorDown);
                                newPotionCauldron.setColor(cauldronColorAbove);
                                if (CauldronFix.canIncrementFluidLevel(state) && world.getBlockState(pos).getBlock() instanceof PotionCauldron) {
                                    for (StatusEffectInstance effectInstance : statusEffectInstanceList) {
                                        newPotionCauldron.addStatusEffect(effectInstance);
                                    }
                                }
                            }
                        } else if (downBlockState.getBlock().equals(ModBlocks.POTION_CAULDRON)) {
                            CauldronFix.incrementFluidLevel(downBlockState.with(PotionCauldron.LIGHT_LEVEL, aboveBlockState.get(PotionCauldron.LIGHT_LEVEL)), world, downPos, false, 1);

                            PotionCauldronBlockEntity potionCauldronBlockEntityDown = (PotionCauldronBlockEntity) world.getBlockEntity(downPos);
                            int cauldronColorDown = potionCauldronBlockEntityDown.getCauldronColor();

                            if (world.getBlockEntity(downPos) instanceof PotionCauldronBlockEntity newPotionCauldron) {
                                newPotionCauldron.setColor(cauldronColorDown);
                                newPotionCauldron.setColor(cauldronColorAbove);
                                for (StatusEffectInstance effectInstance : statusEffectInstanceList) {
                                    newPotionCauldron.addStatusEffect(effectInstance);
                                }
                            }
                        }else if(downBlockState.getBlock() == Blocks.WATER_CAULDRON){

                            world.setBlockState(downPos, ModBlocks.POTION_CAULDRON.getDefaultState().with(PotionCauldron.LEVEL, downBlockState.get(Properties.LEVEL_3)).with(PotionCauldron.LIGHT_LEVEL, aboveBlockState.get(PotionCauldron.LIGHT_LEVEL)));
                            CauldronFix.incrementFluidLevel(world.getBlockState(downPos), world, downPos, false, 1);
                            if (world.getBlockEntity(downPos) instanceof PotionCauldronBlockEntity newPotionCauldron) {
                                newPotionCauldron.setColor(cauldronColorAbove);

                                for (StatusEffectInstance effectInstance : statusEffectInstanceList) {
                                    newPotionCauldron.addStatusEffect(effectInstance);
                                }
                            }
                        }
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);

                    }
                    world.updateListeners(pos, state, state, 0);
                    world.markDirty(pos);
                    return 16;
                }else{

                    world.setBlockState(downPos, ModBlocks.POTION_CAULDRON.getDefaultState().with(PotionCauldron.LIGHT_LEVEL, aboveBlockState.get(PotionCauldron.LIGHT_LEVEL)));
                    CauldronFix.incrementFluidLevel(downBlockState, world, downPos, false, 1);

                    if (world.getBlockEntity(downPos) instanceof PotionCauldronBlockEntity newPotionCauldron) {
                        newPotionCauldron.setColor(cauldronColorAbove);
                        if (CauldronFix.canIncrementFluidLevel(state) && world.getBlockState(pos).getBlock() instanceof PotionCauldron) {
                            for (StatusEffectInstance effectInstance : statusEffectInstanceList) {
                                newPotionCauldron.addStatusEffect(effectInstance);
                            }
                        }
                    }
                    CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                    world.updateListeners(pos, state, state, 0);
                    world.markDirty(pos);
                    return 16;
                }
            }
        } else if (aboveBlockState.getBlock() == ModBlocks.HONEY_CAULDRON) {


            if (cauldronTransferCooldown <= 0 && (downBlockState.getBlock() == ModBlocks.HONEY_CAULDRON || downBlockState.getBlock() == Blocks.CAULDRON)) {


                if (downBlockState.getBlock() != Blocks.CAULDRON) {
                    int downWaterLevel = downBlockState.get(Properties.LEVEL_3);
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);

                    if (downWaterLevel < 3 && aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        CauldronFix.incrementFluidLevel(downBlockState, world, downPos, false, 1);
                    }
                    world.markDirty(pos);
                    return 16;
                } else {
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);
                    if (aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        world.setBlockState(downPos, ModBlocks.HONEY_CAULDRON.getDefaultState());
                        return 16;
                    }
                }

            }
        } else if (aboveBlockState.getBlock() == ModBlocks.BAD_OMEN_CAULDRON) {


            if (cauldronTransferCooldown <= 0 && (downBlockState.getBlock() == ModBlocks.BAD_OMEN_CAULDRON || downBlockState.getBlock() == Blocks.CAULDRON)) {


                if (downBlockState.getBlock() != Blocks.CAULDRON) {
                    int downWaterLevel = downBlockState.get(Properties.LEVEL_3);
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);

                    if (downWaterLevel < 3 && aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        CauldronFix.incrementFluidLevel(downBlockState, world, downPos, false, 1);
                    }
                    world.markDirty(pos);
                    return 16;
                } else {
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);
                    if (aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        world.setBlockState(downPos, ModBlocks.BAD_OMEN_CAULDRON.getDefaultState());
                        return 16;
                    }
                }

            }
        } else if (aboveBlockState.getBlock() == ModBlocks.DRAGONS_BREATH_CAULDRON) {


            if (cauldronTransferCooldown <= 0 && (downBlockState.getBlock() == ModBlocks.DRAGONS_BREATH_CAULDRON || downBlockState.getBlock() == Blocks.CAULDRON)) {


                if (downBlockState.getBlock() != Blocks.CAULDRON) {
                    int downWaterLevel = downBlockState.get(Properties.LEVEL_3);
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);

                    if (downWaterLevel < 3 && aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        CauldronFix.incrementFluidLevel(downBlockState, world, downPos, false, 1);
                    }
                    world.markDirty(pos);
                    return 8;
                } else {
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);
                    if (aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        world.setBlockState(downPos, ModBlocks.DRAGONS_BREATH_CAULDRON.getDefaultState());
                        return 8;
                    }
                }

            }
        } else if (aboveBlockState.getBlock() == Blocks.LAVA_CAULDRON) {

            if (cauldronTransferCooldown <= 0 && downBlockState.isOf(Blocks.CAULDRON)) {

                world.setBlockState(abovePos, Blocks.CAULDRON.getDefaultState());
                world.setBlockState(downPos, Blocks.LAVA_CAULDRON.getDefaultState());
                world.markDirty(pos);
                return 16;
            }
        } else if (aboveBlockState.getBlock() == ModBlocks.MILK_CAULDRON) {


            if (cauldronTransferCooldown <= 0 && downBlockState.isOf(Blocks.CAULDRON)) {

                world.setBlockState(abovePos, Blocks.CAULDRON.getDefaultState());
                world.setBlockState(downPos, ModBlocks.MILK_CAULDRON.getDefaultState());
                world.markDirty(pos);
                return 8;
            }
        }
        return 0;
    }

    private static BlockPos isPointingToAnotherHopper(World world, BlockPos pos, BlockState stateOrg, Integer maxRange, Integer orgMaxRange, ParticleEffect particle) {

        if (world.isReceivingRedstonePower(pos)) {
            return pos;
        }
        if (stateOrg.getBlock() instanceof HopperBlock && maxRange > 0) {

            BlockPos posnew = pos.offset(stateOrg.get(HopperBlock.FACING));
            stateOrg = world.getBlockState(posnew);
            int newMaxRange = maxRange - 1;
            if (posnew.getY() < pos.getY()) {
                newMaxRange = orgMaxRange;
            }

            if (particle != null && world.getBlockState(pos.down()).getBlock() == Blocks.AIR && world.isClient) {

                assert MinecraftClient.getInstance().world != null;
                MinecraftClient.getInstance().world.addParticle(particle, pos.toBottomCenterPos().getX(), pos.toBottomCenterPos().getY(), pos.toBottomCenterPos().getZ(), 0, 0, 0);
            }

            if (!(posnew == pos)) {
                return isPointingToAnotherHopper(world, posnew, stateOrg, newMaxRange, orgMaxRange, particle);
            }
        }
        return pos;
    }

    private static ParticleEffect getParticle(Block block) {
        if (block == Blocks.WATER_CAULDRON) {
            return ParticleTypes.DRIPPING_WATER;
        } else if (block == Blocks.LAVA_CAULDRON) {
            return ParticleTypes.DRIPPING_LAVA;
        } else if (block == ModBlocks.HONEY_CAULDRON) {
            return ParticleTypes.DRIPPING_HONEY;
        } else if (block == ModBlocks.DRAGONS_BREATH_CAULDRON) {
            return ParticleTypes.DRIPPING_OBSIDIAN_TEAR;
        } else if (block == Blocks.LAVA) {
            return ParticleTypes.DRIPPING_LAVA;
        } else if (block == Blocks.WATER) {
            return ParticleTypes.DRIPPING_WATER;
        }
        return null;
    }
}

package mypals.ml.HopperTransportManage;

import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlocks;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldronBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

public class HopperTransportManager {
    public static int hopperTransfer(Integer cauldronTransferCooldown, World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci){



        if(world.isReceivingRedstonePower(pos)) {return 0;}
        BlockPos abovePos = pos.up();
        BlockState aboveBlockState = world.getBlockState(abovePos);

        if(!(aboveBlockState.getBlock() == Blocks.WATER_CAULDRON)&&
                !(aboveBlockState.getBlock() == Blocks.LAVA_CAULDRON)&&
                        !(aboveBlockState.getBlock() == ModBlocks.HONEY_CAULDRON)&&
                                !(aboveBlockState.getBlock() == ModBlocks.MILK_CAULDRON)&&
                                        !(aboveBlockState.getBlock() == ModBlocks.DRAGONS_BREATH_CAULDRON)&&
                                                !(aboveBlockState.getBlock() == ModBlocks.COLORED_CAULDRON)&&
                                                        !(aboveBlockState.getBlock() == ModBlocks.BAD_OMEN_CAULDRON))

         {return 0;}
        
        BlockPos downPos = pos.offset(state.get(HopperBlock.FACING));
        BlockState downBlockState = world.getBlockState(downPos);

        if(downBlockState.getBlock() instanceof HopperBlock)
        {
            downPos = isPointingToAnotherHopper(world,downPos,downBlockState,8,8,getParticle(world.getBlockState(abovePos).getBlock()));
            downBlockState = world.getBlockState(downPos);
        }

        if (aboveBlockState.getBlock() == Blocks.WATER_CAULDRON) {

            if (cauldronTransferCooldown <= 0 && (downBlockState.getBlock() == Blocks.WATER_CAULDRON || downBlockState.getBlock() == Blocks.CAULDRON)) {


                if(downBlockState.getBlock() != Blocks.CAULDRON) {
                    int downWaterLevel = downBlockState.get(Properties.LEVEL_3);
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);

                    if (downWaterLevel < 3 && aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        CauldronFix.incrementFluidLevel(downBlockState, world, downPos, false, 1);
                    }
                    world.markDirty(pos);
                    return 8;
                }else{
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);
                    if(aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        world.setBlockState(downPos, Blocks.WATER_CAULDRON.getDefaultState());
                        return 8;
                    }
                }

            }
        }else if (aboveBlockState.getBlock() == ModBlocks.COLORED_CAULDRON) {
            if (cauldronTransferCooldown <=0 && (downBlockState.getBlock() == ModBlocks.COLORED_CAULDRON || downBlockState.getBlock() == Blocks.CAULDRON)) {


                if(downBlockState.getBlock() != Blocks.CAULDRON) {
                    int downWaterLevel = downBlockState.get(Properties.LEVEL_3);
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);


                    if (downWaterLevel < 3 && aboveWaterLevel > 0) {
                        ColoredCauldronBlockEntity coloredCauldronBlockAbove = (ColoredCauldronBlockEntity)world.getBlockEntity(abovePos);
                        int color = coloredCauldronBlockAbove.getCauldronColor();
                        downBlockState.with(ColoredCauldron.LIGHT_LEVEL,aboveBlockState.get(ColoredCauldron.LIGHT_LEVEL));
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        CauldronFix.incrementFluidLevel(downBlockState.with(ColoredCauldron.LIGHT_LEVEL,aboveBlockState.get(ColoredCauldron.LIGHT_LEVEL)), world, downPos, false, 1);

                        if(downBlockState.getBlock() == ModBlocks.COLORED_CAULDRON){

                            ColoredCauldronBlockEntity coloredCauldronBlockDown = (ColoredCauldronBlockEntity)world.getBlockEntity(downPos);
                            if(coloredCauldronBlockDown == null) return 16;
                            if(color != coloredCauldronBlockDown.getCauldronColor()){
                                coloredCauldronBlockDown.setColor(coloredCauldronBlockAbove.getCauldronColor());
                            }
                        }
                    }
                    world.markDirty(pos);
                    return 16;
                }else{
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);
                    if(aboveWaterLevel > 0) {
                        ColoredCauldronBlockEntity coloredCauldronBlockAbove = (ColoredCauldronBlockEntity)world.getBlockEntity(abovePos);

                        int color = coloredCauldronBlockAbove.getCauldronColor();

                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        world.setBlockState(downPos, ModBlocks.COLORED_CAULDRON.getDefaultState().with(ColoredCauldron.LIGHT_LEVEL,aboveBlockState.get(ColoredCauldron.LIGHT_LEVEL)));

                        ColoredCauldronBlockEntity coloredCauldronBlockDown = (ColoredCauldronBlockEntity)world.getBlockEntity(downPos);
                        if(coloredCauldronBlockDown == null) return 16;
                        if(color != coloredCauldronBlockDown.getCauldronColor()){
                            coloredCauldronBlockDown.setColor(color);
                        }

                        return 16;
                    }
                }

            }
        }

        else if (aboveBlockState.getBlock() == ModBlocks.HONEY_CAULDRON) {


            if (cauldronTransferCooldown <=0 && (downBlockState.getBlock() == ModBlocks.HONEY_CAULDRON || downBlockState.getBlock() == Blocks.CAULDRON)) {


                if(downBlockState.getBlock() != Blocks.CAULDRON) {
                    int downWaterLevel = downBlockState.get(Properties.LEVEL_3);
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);

                    if (downWaterLevel < 3 && aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        CauldronFix.incrementFluidLevel(downBlockState, world, downPos, false, 1);
                    }
                    world.markDirty(pos);
                    return 16;
                }else{
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);
                    if(aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        world.setBlockState(downPos, ModBlocks.HONEY_CAULDRON.getDefaultState());
                        return 16;
                    }
                }

            }
        }

        else if (aboveBlockState.getBlock() == ModBlocks.BAD_OMEN_CAULDRON) {


            if (cauldronTransferCooldown <=0 && (downBlockState.getBlock() == ModBlocks.BAD_OMEN_CAULDRON || downBlockState.getBlock() == Blocks.CAULDRON)) {


                if(downBlockState.getBlock() != Blocks.CAULDRON) {
                    int downWaterLevel = downBlockState.get(Properties.LEVEL_3);
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);

                    if (downWaterLevel < 3 && aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        CauldronFix.incrementFluidLevel(downBlockState, world, downPos, false, 1);
                    }
                    world.markDirty(pos);
                    return 16;
                }else{
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);
                    if(aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        world.setBlockState(downPos, ModBlocks.BAD_OMEN_CAULDRON.getDefaultState());
                        return 16;
                    }
                }

            }
        }

        else if (aboveBlockState.getBlock() == ModBlocks.DRAGONS_BREATH_CAULDRON) {


            if (cauldronTransferCooldown <=0 && (downBlockState.getBlock() == ModBlocks.DRAGONS_BREATH_CAULDRON || downBlockState.getBlock() == Blocks.CAULDRON)) {


                if(downBlockState.getBlock() != Blocks.CAULDRON) {
                    int downWaterLevel = downBlockState.get(Properties.LEVEL_3);
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);

                    if (downWaterLevel < 3 && aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        CauldronFix.incrementFluidLevel(downBlockState, world, downPos, false, 1);
                    }
                    world.markDirty(pos);
                    return 8;
                }else{
                    int aboveWaterLevel = aboveBlockState.get(Properties.LEVEL_3);
                    if(aboveWaterLevel > 0) {
                        CauldronFix.decrementFluidLevel(aboveBlockState, world, abovePos, false, 1);
                        world.setBlockState(downPos, ModBlocks.DRAGONS_BREATH_CAULDRON.getDefaultState());
                        return 8;
                    }
                }

            }
        }

        else if (aboveBlockState.getBlock() == Blocks.LAVA_CAULDRON) {

            if (cauldronTransferCooldown <=0 && downBlockState.isOf(Blocks.CAULDRON)) {

                world.setBlockState(abovePos, Blocks.CAULDRON.getDefaultState());
                world.setBlockState(downPos, Blocks.LAVA_CAULDRON.getDefaultState());
                world.markDirty(pos);
                return 16;
            }
        }
        else if (aboveBlockState.getBlock() == ModBlocks.MILK_CAULDRON) {


            if (cauldronTransferCooldown <=0 && downBlockState.isOf(Blocks.CAULDRON)) {

                world.setBlockState(abovePos, Blocks.CAULDRON.getDefaultState());
                world.setBlockState(downPos, ModBlocks.MILK_CAULDRON.getDefaultState());
                world.markDirty(pos);
                return 8;
            }
        }
        return 0;
    }
    private static BlockPos isPointingToAnotherHopper(World world, BlockPos pos, BlockState stateOrg, Integer maxRange, Integer orgMaxRange, ParticleEffect particle){

        if(world.isReceivingRedstonePower(pos)) {return pos;}
        if(stateOrg.getBlock() instanceof HopperBlock && maxRange > 0) {

            BlockPos posnew = pos.offset(stateOrg.get(HopperBlock.FACING));
            stateOrg = world.getBlockState(posnew);
            int newMaxRange = maxRange - 1;
            if (posnew.getY() < pos.getY() ){
                newMaxRange = orgMaxRange;
            }

            if(particle != null && world.getBlockState(pos.down()).getBlock() == Blocks.AIR && world.isClient) {

                assert MinecraftClient.getInstance().world != null;
                MinecraftClient.getInstance().world.addParticle(particle, pos.toBottomCenterPos().getX(), pos.toBottomCenterPos().getY(), pos.toBottomCenterPos().getZ(), 0, 0, 0);
            }

            if (!(posnew == pos)) {
                return isPointingToAnotherHopper(world, posnew, stateOrg, newMaxRange, orgMaxRange, particle);
            }
        }
        return pos;
    }
    private static ParticleEffect getParticle(Block block){
        if(block == Blocks.WATER_CAULDRON)
        {
            return ParticleTypes.DRIPPING_WATER;
        }else if(block == Blocks.LAVA_CAULDRON)
        {
            return ParticleTypes.DRIPPING_LAVA;
        }else if(block == ModBlocks.HONEY_CAULDRON)
        {
            return ParticleTypes.DRIPPING_HONEY;
        }else if(block == ModBlocks.DRAGONS_BREATH_CAULDRON)
        {
            return ParticleTypes.DRIPPING_OBSIDIAN_TEAR;
        }else if(block == Blocks.LAVA)
        {
            return ParticleTypes.DRIPPING_LAVA;
        }else if(block == Blocks.WATER)
        {
            return ParticleTypes.DRIPPING_WATER;
        }
        return null;
    }
}

package mypals.ml;

import mypals.ml.block.ModBlockEntityTypes;
import mypals.ml.block.ModBlocks;
import mypals.ml.block.advancedCauldron.BehaciorMaps;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldronBlockEntity;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldronBlockEntity;
import mypals.ml.item.ModItemGroups;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CauldronFix implements ModInitializer {

    public static final String MOD_ID = "cauldronfix";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        ModBlocks.registerModBlocks();
        ModItemGroups.registerModItemGroups();
        BehaciorMaps.registerBehaviorMaps();
        ModBlockEntityTypes.registerBlockEntities();
        LOGGER.info("Hello Fabric world!");
        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {

            BlockPos clickedBlockPos = hitResult.getBlockPos();

            Direction side = hitResult.getSide();

            BlockPos placeBlockPos = clickedBlockPos.offset(side);
            CauldronBlockWatcher.cauldronBlockCheck(world, placeBlockPos);
            return ActionResult.PASS;
        });
    }

    public static boolean decrementFluidLevel(BlockState state, World world, BlockPos pos, boolean required, int amount) {
        return setFluidLevel(state, world, pos, required, getFluidLevel(state) - amount);
    }

    public static boolean incrementFluidLevel(BlockState state, World world, BlockPos pos, boolean required, int amount) {
        return setFluidLevel(state, world, pos, required, getFluidLevel(state) + amount);
    }

    public static boolean decrementFluidLevel(BlockState state, World world, BlockPos pos) {
        return decrementFluidLevel(state, world, pos, true, 1);
    }

    public static boolean incrementFluidLevel(BlockState state, World world, BlockPos pos) {
        return incrementFluidLevel(state, world, pos, true, 1);
    }

    public static int getFluidLevel(BlockState state) {
        if (state.isOf(Blocks.LAVA_CAULDRON)) {
            return 1;
        } else if (state.getBlock() instanceof LeveledCauldronBlock) {
            return state.get(LeveledCauldronBlock.LEVEL);
        } else if (state.isOf(Blocks.CAULDRON)) {
            return 0;
        }
        return -1;
    }

    public static int getMaxFluidLevel(BlockState state) {
        if (state.getBlock() instanceof LeveledCauldronBlock block) {
            return block.MAX_LEVEL;
        } else if (state.isOf(Blocks.LAVA_CAULDRON)) {
            return 1;
        } else if (state.getBlock() instanceof LeveledCauldronBlock) {
            return 3;
        } else if (state.isOf(Blocks.CAULDRON)) {
            return 0;
        }
        return -1;
    }

    public static boolean canSetFluidLevel(BlockState state, int level) {
        int maxLevel = getMaxFluidLevel(state);
        int actualLevel = Math.max(0, Math.min(level, maxLevel));

        return maxLevel != -1 && level == actualLevel && getFluidLevel(state) != actualLevel;
    }

    public static boolean canIncrementFluidLevel(BlockState state, int amount) {
        return canSetFluidLevel(state, getFluidLevel(state) + amount);
    }

    public static boolean canIncrementFluidLevel(BlockState state) {
        return canIncrementFluidLevel(state, 1);
    }

    public static boolean setFluidLevel(BlockState state, World world, BlockPos pos, boolean required, int level) {
        int maxLevel = getMaxFluidLevel(state);
        int actualLevel = Math.max(0, Math.min(level, maxLevel));

        if (maxLevel == -1 || (level != actualLevel && required) || getFluidLevel(state) == actualLevel) return false;

        if (state.isOf(Blocks.LAVA_CAULDRON) && actualLevel == 0) {
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            CauldronBlockWatcher.cauldronBlockCheck(world, pos);
            return true;
        } else if (state.getBlock() instanceof LeveledCauldronBlock) {

            world.setBlockState(pos, actualLevel == 0 ? Blocks.CAULDRON.getDefaultState() : state.with(LeveledCauldronBlock.LEVEL, actualLevel));
            CauldronBlockWatcher.cauldronBlockCheck(world, pos);
            return true;
        }

        return false;
    }

    public static CauldronBehavior createFillFromBottleBehavior(Block cauldron, SoundEvent bottleEmptySound) {
        return (state, world, pos, player, hand, stack) -> {
            if (!world.isClient) {
                Item item = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                world.setBlockState(pos, cauldron.getDefaultState());
                CauldronBlockWatcher.cauldronBlockCheck(world, pos);
                world.playSound(null, pos, bottleEmptySound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }

            return ItemActionResult.success(world.isClient);
        };
    }

    public static CauldronBehavior createDyeBehavior(Block cauldron, SoundEvent dyeSound) {
        return (state, world, pos, player, hand, stack) -> {
			/*if (!world.isClient) {
				BlockEntity blockEntity = world.getBlockEntity (pos);
				if(world.getBlockState(pos).getBlock() != ModBlocks.COLORED_CAULDRON){
					world.setBlockState(pos, cauldron.getDefaultState().with(Properties.LEVEL_3, state.get(Properties.LEVEL_3)));
					blockEntity = world.getBlockEntity (pos);
				}

				Item item = stack.getItem();

				if (item instanceof DyeItem dyeItem && blockEntity instanceof ColoredCauldronBlockEntity coloredCauldron) {
					player.swingHand(hand);
					coloredCauldron.resetColor();

					coloredCauldron.setColor(dyeItem.getColor());
					coloredCauldron.toUpdatePacket();
					CauldronFix.rebuildBlock(pos);
					world.playSound(null, pos, dyeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
					LOGGER.info("Dyeing...");
					if(!player.isCreative() && !player.isSpectator()) {
						stack.decrement(1);
					}
					return ItemActionResult.success(world.isClient);
				}
				CauldronFix.rebuildBlock(pos);
				CauldronBlockWatcher.cauldronBlockCheck(world,pos);
				world.playSound(null, pos, dyeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
				world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
			}else{
				CauldronFix.rebuildBlock(pos);
			}*/
            world.updateListeners(pos, state, state, 0);
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    public static CauldronBehavior createFillFromBucketBehavior(Block cauldron, SoundEvent bucketEmptySound) {
        return (state, world, pos, player, hand, stack) -> CauldronBehavior.fillCauldron(world, pos, player, hand, stack, cauldron.getDefaultState(), bucketEmptySound);
    }

    @Environment(EnvType.CLIENT)
    public static int getColor(BlockRenderView world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof ColoredCauldronBlockEntity colorCauldron) {
            int color = colorCauldron.getCauldronColor();
            if (color != -1)
                return colorCauldron.getCauldronColor();
        } else if (blockEntity instanceof PotionCauldronBlockEntity potionCauldron) {
            int color = potionCauldron.getCauldronColor();
            if (color != -1)
                return potionCauldron.getCauldronColor();
        }
        return BiomeColors.getWaterColor(world, pos);
    }

    @Environment(EnvType.CLIENT)
    public static void rebuildBlock(BlockPos pos) {
        MinecraftClient.getInstance().worldRenderer.scheduleBlockRenders(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }
}
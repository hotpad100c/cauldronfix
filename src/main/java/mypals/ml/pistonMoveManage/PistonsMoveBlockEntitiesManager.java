package mypals.ml.pistonMoveManage;

import com.google.common.collect.Lists;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldron;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
//Copied from Quark, doesn't work, has compatibility issues, abandoned for now
public class PistonsMoveBlockEntitiesManager {
    private static final WeakHashMap<World, Map<BlockPos, NbtCompound>> movements = new WeakHashMap<>();

    public static boolean canMove(boolean prev, BlockState blockState){
        Optional<RegistryKey<Block>> res = Registries.BLOCK.getKey(blockState.getBlock());
        if(res.isEmpty())
            return true;
        if(blockState.getBlock() == Blocks.PISTON_HEAD)
            return true;
        if((blockState.getBlock() instanceof ColoredCauldron || blockState.getBlock() instanceof PotionCauldron))
            return false;
        else if(blockState.getBlock() instanceof BlockWithEntity)
            return true;
        return false;
    }
    public static void detachTileEntities(World world, PistonHandler helper, Direction facing, boolean extending) {
        if(!extending)
            facing = facing.getOpposite();

        List<BlockPos> moveList = helper.getMovedBlocks();

        for(BlockPos pos : moveList) {
            BlockState state = world.getBlockState(pos);
            if(state.getBlock() instanceof BlockWithEntity) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if(blockEntity != null) {
                    NbtCompound nbt = blockEntity.createNbtWithIdentifyingData(Objects.requireNonNull(blockEntity.getWorld()).getRegistryManager());
                    setMovingBlockEntityData(world, pos.offset(facing), nbt);
                    world.removeBlockEntity(pos);
                }
            }
        }
    }
    public static void setMovingBlockEntityData(World world, BlockPos pos, NbtCompound nbt) {
        movements.computeIfAbsent(world, l -> new HashMap<>()).put(pos, nbt);
    }
    public static boolean setPistonBlock(World world, BlockPos pos, BlockState state, int flags) {
        Block block = state.getBlock();
        NbtCompound entityTag = getAndClearMovement(world, pos);
        boolean destroyed = false;

        if (entityTag != null) {
            world.removeBlock(pos, false);
        }
        if (entityTag != null && !world.isClient()) {
            loadBlockEntitySafe(world, pos, entityTag);
        }

        if(!destroyed) {
            world.setBlockState(pos, state, flags);

            if(world.getBlockEntity(pos) != null)
                world.setBlockState(pos, state, 0);

            world.updateNeighbor(pos, block, null);
        }

        return true;
    }
    private static NbtCompound getAndClearMovement(World world, BlockPos pos) {
        return getMovingBlockEntityData(world, pos, true);
    }
    private static NbtCompound getMovingBlockEntityData(World world, BlockPos pos, boolean remove) {
        if(!movements.containsKey(world))
            return null;

        Map<BlockPos, NbtCompound> worldMovements = movements.get(world);
        if(!worldMovements.containsKey(pos))
            return null;

        NbtCompound ret = worldMovements.get(pos);
        if(remove)
            worldMovements.remove(pos);

        return ret;
    }
    @Nullable
    private static BlockEntity loadBlockEntitySafe(World level, BlockPos pos, NbtCompound tag) {
        BlockEntity inWorldEntity = level.getBlockEntity(pos);
        String expectedTypeStr = tag.getString("id");
        if(inWorldEntity == null) {
            return null;
        } else if(inWorldEntity.getType() != Registries.BLOCK_ENTITY_TYPE.get(Identifier.of(expectedTypeStr))) {
            return null;
        } else {
            inWorldEntity.read(tag,inWorldEntity.getWorld().getRegistryManager());
            inWorldEntity.markDirty();
            return inWorldEntity;
        }
    }
}


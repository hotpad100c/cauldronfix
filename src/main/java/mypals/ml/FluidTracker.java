package mypals.ml;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class FluidTracker {
    public static BlockPos findLavaFluidSource(World world, BlockPos startPos) {
        BlockPos currentPos = startPos;
        List<BlockPos> checked = new ArrayList<>();
        for (int i = 0; i<114514; i++) {
            BlockState blockState = world.getBlockState(currentPos);
            FluidState fluidState = blockState.getFluidState();

            if (fluidState.getFluid() == Fluids.LAVA) {
                return currentPos;
            }

            BlockPos nextPos = getNextFluidBlock(world, currentPos,checked);
            if (nextPos == null) {
                return null;
            }
            checked.add(currentPos);
            currentPos = nextPos;

        }

        return null;
    }

    private static BlockPos getNextFluidBlock(World world, BlockPos currentPos, List<BlockPos> checked) {

        for (BlockPos pos : BlockPos.iterateOutwards(currentPos, 1, 1, 1)) {
            if (!pos.equals(currentPos) && pos.getY() >= currentPos.getY() && !checked.contains(pos)) {
                BlockState neighborBlockState = world.getBlockState(pos);
                FluidState neighborFluidState = neighborBlockState.getFluidState();
                if (!neighborFluidState.isEmpty()) {
                    return pos;
                }
            }
        }
        return null;
    }
}

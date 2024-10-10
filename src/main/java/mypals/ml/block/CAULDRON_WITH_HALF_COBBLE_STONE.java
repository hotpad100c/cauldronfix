package mypals.ml.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CAULDRON_WITH_HALF_COBBLE_STONE extends Block {

    public CAULDRON_WITH_HALF_COBBLE_STONE(Settings settings) {
        super(settings);
    }
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(
                VoxelShapes.cuboid(0, 0.1875, 0, 0.125, 1, 1),
                VoxelShapes.cuboid(0.125, 0.1875, 0.125, 0.875, 0.25, 0.875),
                VoxelShapes.cuboid(0.875, 0.1875, 0, 1, 1, 1),
                VoxelShapes.cuboid(0.125, 0.1875, 0, 0.875, 1, 0.125),
                VoxelShapes.cuboid(0.125, 0.1875, 0.875, 0.875, 1, 1),
                VoxelShapes.cuboid(0, 0, 0, 0.25, 0.1875, 0.125),
                VoxelShapes.cuboid(0, 0, 0.125, 0.125, 0.1875, 0.25),
                VoxelShapes.cuboid(0.75, 0, 0, 1, 0.1875, 0.125),
                VoxelShapes.cuboid(0.875, 0, 0.125, 1, 0.1875, 0.25),
                VoxelShapes.cuboid(0, 0, 0.875, 0.25, 0.1875, 1),
                VoxelShapes.cuboid(0, 0, 0.75, 0.125, 0.1875, 0.875),
                VoxelShapes.cuboid(0.75, 0, 0.875, 1, 0.1875, 1),
                VoxelShapes.cuboid(0.875, 0, 0.75, 1, 0.1875, 0.875),
                VoxelShapes.cuboid(0.125, 0.25, 0.125, 0.875, 0.5625, 0.875)
        );
    }
    @Override
    protected boolean hasComparatorOutput(BlockState state) {return true;}
    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return 5;
    }
}

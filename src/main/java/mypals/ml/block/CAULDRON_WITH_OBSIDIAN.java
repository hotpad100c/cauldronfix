package mypals.ml.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class CAULDRON_WITH_OBSIDIAN extends Block {
    public CAULDRON_WITH_OBSIDIAN(Settings settings) {
        super(settings);
    }
    /*@Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context){
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
                VoxelShapes.cuboid(0.125, 0.25, 0.125, 0.875, 0.9375, 0.875)
        );
    }*/

}

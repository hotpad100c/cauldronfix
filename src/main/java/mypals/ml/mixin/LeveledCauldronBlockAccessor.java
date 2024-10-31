package mypals.ml.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LeveledCauldronBlock.class)
public interface  LeveledCauldronBlockAccessor {

    @Invoker
    double invokeGetFluidHeight(BlockState state);

}

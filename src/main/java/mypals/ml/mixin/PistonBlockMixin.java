package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import mypals.ml.pistonMoveManage.PistonsMoveBlockEntitiesManager;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
//Copied from Quark, doesn't work, has compatibility issues, abandoned for now
@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @ModifyExpressionValue(method = "isMovable", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;hasBlockEntity()Z"))
    private static boolean isMovable(boolean prev, BlockState blockStateIn) {
        return PistonsMoveBlockEntitiesManager.canMove(prev, blockStateIn);
    }
    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/piston/PistonHandler;getMovedBlocks()Ljava/util/List;"))
    private void move(World world, BlockPos pos, Direction dir, boolean extending, CallbackInfoReturnable<Boolean> cir, @Local PistonHandler pistonHandler) {
        PistonsMoveBlockEntitiesManager.detachTileEntities(world, pistonHandler, dir, extending);
    }

    @ModifyVariable(
            method = "move", at = @At(value = "STORE", ordinal = 0), index = 15, ordinal = 2,
            slice = @Slice(
                    from = @At(value = "INVOKE", target ="Lnet/minecraft/world/World;addBlockBreakParticles(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V" ),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/block/PistonExtensionBlock;createBlockEntityPiston(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;ZZ)Lnet/minecraft/block/entity/BlockEntity;")
            )
    )
    private BlockPos storeOldPos(BlockPos pos, @Share("oldPos") LocalRef<BlockPos> oldPos) {
        oldPos.set(pos);
        return pos;
    }

    @ModifyVariable(method = "move", at = @At(value = "STORE", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/block/piston/PistonHandler;calculatePush()Z"), to = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", remap = false)))
    private Map<BlockPos, BlockState> storeMap(Map<BlockPos, BlockState> map, @Share("storedMap") LocalRef<Map<BlockPos, BlockState>> storedMap) {
        storedMap.set(map);
        return map;
    }
    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 2, shift = At.Shift.AFTER))
    private void modifyBlockstate(World worldIn, BlockPos posIn, Direction pistonFacing, boolean extending, CallbackInfoReturnable<Boolean> cir, @Share("oldPos") LocalRef<BlockPos> oldPos, @Share("newState") LocalRef<BlockState> newState, @Share("storedMap") LocalRef<Map<BlockPos, BlockState>> storedMap) {
        newState.set(worldIn.getBlockState(oldPos.get()));
        storedMap.get().replace(oldPos.get(), newState.get());
    }


    @ModifyArg(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/PistonExtensionBlock;createBlockEntityPiston(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;ZZ)Lnet/minecraft/block/entity/BlockEntity;", ordinal = 0), index = 2)
    private BlockState modifyMovingBlockEntityState(BlockState state, @Share("newState") LocalRef<BlockState> newState) {
        return newState.get();
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 0, shift = At.Shift.AFTER))
    private void setOldPosToAir(World world, BlockPos pos, Direction directionIn, boolean extending, CallbackInfoReturnable<Boolean> cir, @Share("oldPos") LocalRef<BlockPos> oldPos) {
        world.setBlockState(oldPos.get(), Blocks.AIR.getDefaultState(), 2 | 4 | 16 | 1024);
    }
}

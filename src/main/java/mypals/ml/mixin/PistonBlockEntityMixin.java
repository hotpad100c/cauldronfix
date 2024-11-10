package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mypals.ml.pistonMoveManage.PistonsMoveBlockEntitiesManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonBlockEntity.class)
//Copied from Quark, doesn't work, has compatibility issues, abandoned for now
public class PistonBlockEntityMixin {

    @WrapOperation(method = { "tick", "finish" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private static boolean tick(World instance, BlockPos pos, BlockState newState, int flags, Operation<Boolean> original) {
        return PistonsMoveBlockEntitiesManager.setPistonBlock(instance, pos, newState, flags) || original.call(instance, pos, newState, flags);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=84"))
    private static int forceNotifyBlockUpdate(int original) {
        // paper impl comment: Paper - force notify (original 2),
        // it's possible the set type by the piston block (which doesn't notify) to set this block to air
        return (original | 2);
    }
}

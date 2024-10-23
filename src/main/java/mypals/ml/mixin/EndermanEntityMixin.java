package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EndermanEntity.class)
public class EndermanEntityMixin {
    @WrapMethod(method = "isPlayerStaring")
    boolean isPlayerStaring(PlayerEntity player, Operation<Boolean> original) {
        if (player.getInventory().armor.get(3).isOf(Blocks.CAULDRON.asItem())) {
            return false;
        }
        return original.call(player);
    }

}


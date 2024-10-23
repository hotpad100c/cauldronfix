package mypals.ml.mixin;

import mypals.ml.dispenserInteractionManage.DispenserInteraction;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPointer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemDispenserBehavior.class)
public class ItemDispenserBehaviorMixin {

    @Inject(at = @At("HEAD"), method = "dispenseSilently", cancellable = true)
    void dispenseMixin(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        DispenserInteraction.interact(stack,pointer,cir);
    }
}

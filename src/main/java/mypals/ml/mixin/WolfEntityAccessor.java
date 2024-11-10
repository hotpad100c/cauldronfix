package mypals.ml.mixin;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WolfEntity.class)
public interface WolfEntityAccessor {
    @Invoker
     void invokeSetCollarColor(DyeColor color);
}

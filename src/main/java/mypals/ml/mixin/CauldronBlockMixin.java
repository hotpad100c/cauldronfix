package mypals.ml.mixin;

import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CauldronBlock.class)
public class CauldronBlockMixin implements EquipmentSlotProvider {

    @Override
    public EquipmentSlot getPreferredEquipmentSlot(LivingEntity entity, ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

}

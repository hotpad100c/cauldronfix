package mypals.ml.mixin;

import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CauldronBlock.class)
public class CauldronBlockMixin implements Equipment {

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }
}

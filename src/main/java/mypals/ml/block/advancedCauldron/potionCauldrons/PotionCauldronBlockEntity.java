package mypals.ml.block.advancedCauldron.potionCauldrons;

import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlockEntityTypes;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldronBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentChanges;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PotionCauldronBlockEntity extends ColoredCauldronBlockEntity {

    private static final Map<RegistryEntry<StatusEffect>, StatusEffectInstance> statusEffects = Maps.<RegistryEntry<StatusEffect>, StatusEffectInstance>newHashMap();
    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }
    //public @Nullable RegistryEntry<Potion> getPotion() { return statusEffects.get(); }

    public void addStatusEffect(StatusEffectInstance effect) {

        StatusEffectInstance statusEffectInstance = (StatusEffectInstance) statusEffects.get(effect.getEffectType());
        if (statusEffectInstance == null) {
            statusEffects.put(effect.getEffectType(), effect);


        } else {
            statusEffectInstance.upgrade(effect);
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt,registryLookup);
        if (!statusEffects.isEmpty()) {
            NbtList nbtList = new NbtList();

            for (StatusEffectInstance statusEffectInstance : statusEffects.values()) {
                nbtList.add(statusEffectInstance.writeNbt());
            }

            nbt.put("effects", nbtList);
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("effects", NbtElement.LIST_TYPE)) {
            NbtList nbtList = nbt.getList("effects", NbtElement.COMPOUND_TYPE);

            for (int i = 0; i < nbtList.size(); i++) {
                NbtCompound nbtCompound = nbtList.getCompound(i);
                StatusEffectInstance statusEffectInstance = StatusEffectInstance.fromNbt(nbtCompound);
                if (statusEffectInstance != null) {
                    statusEffects.put(statusEffectInstance.getEffectType(), statusEffectInstance);
                }
            }
        }
        markDirty();
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
    

}

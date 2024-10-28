package mypals.ml.block.advancedCauldron.potionCauldrons;

import com.google.common.primitives.Ints;
import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PotionCauldronBlockEntity extends BlockEntity {

    private static final Map<RegistryEntry<StatusEffect>, StatusEffectInstance> NULL_EFFECT = new HashMap<>();
    private static final int[] NULL_COLOR = new int[]{-1, -1, -1};
    private Map<RegistryEntry<StatusEffect>, StatusEffectInstance> cauldronStatusEffects = new HashMap<>();
    private int[] color = NULL_COLOR;

    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.POTION_CAULDRON_BLOCK_ENTITY, pos, state);
    }


    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (cauldronStatusEffects != null && !cauldronStatusEffects.isEmpty()) {
            NbtList nbtList = new NbtList();

            for (StatusEffectInstance statusEffectInstance : cauldronStatusEffects.values()) {
                nbtList.add(statusEffectInstance.writeNbt());
            }

            nbt.put("potionEffects", nbtList);
        }
        nbt.putIntArray("color", color);
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (nbt.contains("potionEffects", NbtElement.LIST_TYPE)) {
            NbtList nbtList = nbt.getList("potionEffects", NbtElement.COMPOUND_TYPE);

            for (int i = 0; i < nbtList.size(); i++) {
                NbtCompound nbtCompound = nbtList.getCompound(i);
                StatusEffectInstance statusEffectInstance = StatusEffectInstance.fromNbt(nbtCompound);
                if (statusEffectInstance != null) {
                    cauldronStatusEffects.put(statusEffectInstance.getEffectType(), statusEffectInstance);
                }
            }
        }
        color = nbt.getIntArray("color");
        super.readNbt(nbt, registryLookup);
        markDirty();
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
    @Nullable
    @Override
    public net.minecraft.network.packet.Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }


    @Override
    public void markDirty() {
        if (world != null) {
            if (world.isClient()) {
                this.toUpdatePacket();
                CauldronFix.rebuildBlock(pos);
            } else if (world instanceof ServerWorld) {
                ((ServerWorld) world).getChunkManager().markForUpdate(pos);
            }
            super.markDirty();
        }
    }
    public void setColor(int dyeColor) {

        int newRed = (dyeColor >> 16) & 0xFF;
        int newGreen = (dyeColor >> 8) & 0xFF;
        int newBlue = dyeColor & 0xFF;

        var newColor = new int[3];
        newColor[0] = newRed;
        newColor[1] = newGreen;
        newColor[2] = newBlue;
        if (!Arrays.equals(color, NULL_COLOR)) {
            var avgColor = new int[3];
            avgColor[0] = (color[0] + newColor[0]) / 3;
            avgColor[1] = (color[1] + newColor[1]) / 3;
            avgColor[2] = (color[2] + newColor[2]) / 3;

            var avgMax = (Ints.max(color) + Ints.max(newColor)) / 2.0f;

            var maxOfAvg = (float) Ints.max(avgColor);
            var gainFactor = avgMax / maxOfAvg;

            color[0] = (int) (avgColor[0] * gainFactor);
            color[1] = (int) (avgColor[1] * gainFactor);
            color[2] = (int) (avgColor[2] * gainFactor);
        } else {
            color = newColor;
        }
        this.toUpdatePacket();
        markDirty();
    }

    public int getCauldronColor() {
        try {
            return !Arrays.equals(color, NULL_COLOR) ? (color[0] << 16) + (color[1] << 8) + color[2] : -1;
        }catch (Exception e){
            CauldronFix.LOGGER.info("Bad color return");
        }
        return -1;
    }
    public void addStatusEffect(StatusEffectInstance effect) {

        StatusEffectInstance statusEffectInstance = cauldronStatusEffects.get(effect.getEffectType());
        if (statusEffectInstance == null) {
            cauldronStatusEffects.put(effect.getEffectType(), effect);


        } else {
            statusEffectInstance.upgrade(effect);
        }
        markDirty();
    }
    public Map<RegistryEntry<StatusEffect>, StatusEffectInstance> getStatusEffect()
    {
        Map<RegistryEntry<StatusEffect>, StatusEffectInstance> effects = cauldronStatusEffects;
        return effects;
    }

}

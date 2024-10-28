package mypals.ml.block.advancedCauldron.coloredCauldrons;

import com.google.common.primitives.Ints;
import com.jcraft.jogg.Packet;
import mypals.ml.CauldronFix;
import mypals.ml.block.ModBlockEntityTypes;
import mypals.ml.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static net.fabricmc.fabric.impl.registry.sync.RegistryMapSerializer.toNbt;

public class ColoredCauldronBlockEntity extends BlockEntity {
    private static final int[] NULL_COLOR = new int[]{-1, -1, -1};
    private int[] color = NULL_COLOR;

    public ColoredCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.COLORED_CAULDRON_BLOCK_ENTITY, pos, state);

    }

    public void setColor(DyeColor dyeColor) {
        setColor(dyeColor.getMapColor().color);
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
        return !Arrays.equals(color, NULL_COLOR) ? (color[0] << 16) + (color[1] << 8) + color[2] : -1;
    }

    public void resetColor() {
        color = NULL_COLOR;
        markDirty();
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putIntArray("color", color);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        color = nbt.getIntArray("color");
        markDirty();
    }

    @Nullable
    @Override
    public net.minecraft.network.packet.Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
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
}

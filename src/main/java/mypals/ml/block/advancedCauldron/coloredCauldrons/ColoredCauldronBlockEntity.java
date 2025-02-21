package mypals.ml.block.advancedCauldron.coloredCauldrons;

import com.google.common.primitives.Ints;
import mypals.ml.CauldronFix;
import mypals.ml.CauldronFixClient;
import mypals.ml.block.ModBlockEntityTypes;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldron;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ColoredCauldronBlockEntity extends BlockEntity {
    private static final int[] NULL_COLOR = new int[]{-1, -1, -1};
    public static final int DEFAULT_COLLIDE_TIME = 200;
    private int[] color = NULL_COLOR;
    private int collideTime = DEFAULT_COLLIDE_TIME;

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
        nbt.putIntArray("color", color);
        nbt.putInt("collideTime", collideTime);
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        color = nbt.getIntArray("color");
        collideTime = nbt.getInt("collideTime");
        markDirty();
        super.readNbt(nbt, registryLookup);
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
                CauldronFixClient.rebuildBlock(pos);
            } else if (world instanceof ServerWorld) {
                ((ServerWorld) world).getChunkManager().markForUpdate(pos);
            }
            super.markDirty();
        }
    }
    public void decreaseColTime (World world,BlockPos blockPos, int number){
        collideTime-= number;
        shouldDropLayer(world, blockPos);
        markDirty();
    }
    private void shouldDropLayer(World world,BlockPos blockPos){
        if(!(world.getBlockState(blockPos).getBlock() instanceof ColoredCauldron)){return;};
        BlockState coloredCauldron = world.getBlockState(blockPos);
        if(collideTime<=0){
            CauldronFix.decrementFluidLevel(coloredCauldron,world,blockPos);
            collideTime = DEFAULT_COLLIDE_TIME;
        }

    }
    public void setColTime(int number){
        collideTime = number;
        markDirty();
    }
    public int getColTime(){
        return collideTime;
    }
}

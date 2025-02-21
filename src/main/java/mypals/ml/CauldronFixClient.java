package mypals.ml;


import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldronBlockEntity;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldronBlockEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;


public class CauldronFixClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

    }
    public static int getColor(BlockRenderView world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof ColoredCauldronBlockEntity colorCauldron) {
            int color = colorCauldron.getCauldronColor();
            if (color != -1)
                return colorCauldron.getCauldronColor();
        } else if (blockEntity instanceof PotionCauldronBlockEntity potionCauldron) {
            int color = potionCauldron.getCauldronColor();
            if (color != -1)
                return potionCauldron.getCauldronColor();
        }
        return BiomeColors.getWaterColor(world, pos);
    }

    public static void rebuildBlock(BlockPos pos) {
        MinecraftClient.getInstance().worldRenderer.scheduleBlockRenders(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }
}

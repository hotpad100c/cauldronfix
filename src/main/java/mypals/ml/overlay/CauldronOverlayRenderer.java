package mypals.ml.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import mypals.ml.block.ModBlocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import static mypals.ml.CauldronFix.MOD_ID;

public class CauldronOverlayRenderer {
    private static final Identifier CAULDRON_OVERLAY = Identifier.of(MOD_ID, "textures/misc/cauldron_overlay.png");
    public static void update(DrawContext draw) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.getPerspective().isFirstPerson()) {
            assert client.player != null;
            Item onHead = client.player.getEquippedStack(EquipmentSlot.HEAD).getItem();
            if (onHead == Items.CAULDRON || onHead == ModBlocks.CAULDRON_WITH_HALF_COBBLE_STONE.asItem() || onHead == ModBlocks.CAULDRON_WITH_EMBER.asItem()) {
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                draw.setShaderColor(1.0f, 1.0f, 1.0f, 1);
                draw.drawTexture(CAULDRON_OVERLAY, 0, 0,-90, 0.0f, 0.0f, draw.getScaledWindowWidth(), draw.getScaledWindowHeight(), draw.getScaledWindowWidth(), draw.getScaledWindowHeight());
                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                draw.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }
}

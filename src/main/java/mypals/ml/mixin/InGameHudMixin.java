package mypals.ml.mixin;
import mypals.ml.overlay.CauldronOverlayRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = {"renderStatusEffectOverlay"}, at = {@At(value = "HEAD")})
    public void Gui(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        CauldronOverlayRenderer.update(context);
    }
}
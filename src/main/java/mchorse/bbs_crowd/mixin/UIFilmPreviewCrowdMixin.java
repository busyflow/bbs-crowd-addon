package mchorse.bbs_crowd.mixin;

import mchorse.bbs_mod.camera.Camera;
import mchorse.bbs_mod.graphics.window.Window;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.UIFilmPreview;
import mchorse.bbs_mod.ui.film.clips.area.AreaBrush;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.utils.Area;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = UIFilmPreview.class, remap = false)
public abstract class UIFilmPreviewCrowdMixin {
    @Shadow
    public UIFilmPanel panel;

    @Shadow
    public Area area;

    @Shadow
    public abstract boolean canBeSeen();

    @Inject(method = "subMouseClicked", at = @At("HEAD"), cancellable = true)
    private void bbs_crowd$onSubMouseClicked(UIContext context, CallbackInfoReturnable<Boolean> cir) {
        if (this.area != null && this.area.isInside(context) && this.panel != null) {
            Camera camera = this.panel.getCamera();
            if (camera != null && AreaBrush.click(context, this.area, camera)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "subMouseReleased", at = @At("HEAD"))
    private void bbs_crowd$onSubMouseReleased(UIContext context, CallbackInfoReturnable<Boolean> cir) {
        AreaBrush.stopPainting();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void bbs_crowd$onRender(UIContext context, CallbackInfo ci) {
        if (AreaBrush.isArmed() && this.canBeSeen() && this.panel != null && this.area != null) {
            boolean left = Window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT);
            boolean right = Window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            Camera camera = this.panel.getCamera();

            if (camera != null) {
                AreaBrush.hover(context, this.area, camera);

                if (this.area.isInside(context) && (left || right)) {
                    AreaBrush.held(context, this.area, camera, right);
                } else {
                    AreaBrush.stopPainting();
                }
            }
        }
    }
}

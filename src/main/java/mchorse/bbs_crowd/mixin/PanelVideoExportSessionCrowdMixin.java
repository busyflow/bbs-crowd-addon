package mchorse.bbs_crowd.mixin;

import mchorse.bbs_mod.actions.types.crowd.CrowdExportPreload;
import mchorse.bbs_mod.ui.film.PanelVideoExportSession;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(value = PanelVideoExportSession.class, remap = false)
public class PanelVideoExportSessionCrowdMixin {
    @Shadow
    private UIFilmPanel editor;

    @Unique
    private String bbs_crowd$preloadFilmId;

    @Inject(method = "start", at = @At("HEAD"))
    private void bbs_crowd$onStart(File file, CallbackInfoReturnable<Boolean> cir) {
        if (this.editor != null && this.editor.getData() != null) {
            this.bbs_crowd$preloadFilmId = this.editor.getData().getId();
            CrowdExportPreload.begin(this.bbs_crowd$preloadFilmId);
        }
    }

    @Inject(method = "isWarmupReady", at = @At("HEAD"), cancellable = true)
    private void bbs_crowd$onIsWarmupReady(CallbackInfoReturnable<Boolean> cir) {
        if (this.bbs_crowd$preloadFilmId != null) {
            cir.setReturnValue(CrowdExportPreload.isReady(this.bbs_crowd$preloadFilmId));
        }
    }

    @Inject(method = "teardown", at = @At("HEAD"))
    private void bbs_crowd$onTeardown(boolean cancelled, CallbackInfo ci) {
        if (this.bbs_crowd$preloadFilmId != null) {
            CrowdExportPreload.finish(this.bbs_crowd$preloadFilmId);
            this.bbs_crowd$preloadFilmId = null;
        }
    }
}

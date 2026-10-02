package mchorse.bbs_crowd.mixin;

import mchorse.bbs_crowd.CrowdSettings;
import mchorse.bbs_mod.actions.types.crowd.CrowdExportPreload;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.VideoExportSession;
import mchorse.bbs_mod.ui.film.PanelVideoExportSession;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PanelVideoExportSession.class, remap = false)
public abstract class PanelVideoExportSessionCrowdMixin extends VideoExportSession {
    @Shadow
    private UIFilmPanel editor;

    @Unique
    private String bbs_crowd$preloadFilmId;

    @Inject(method = "start", at = @At("HEAD"))
    private void bbs_crowd$onStart(int duration, int textureId, int width, int height, CallbackInfoReturnable<Boolean> cir) {
        if (this.editor != null && this.editor.getData() != null && CrowdSettings.isCrowdExportFull()) {
            Film film = this.editor.getData();
            mchorse.bbs_mod.film.crowds.Crowds crowds = mchorse.bbs_crowd.access.FilmCrowdAccess.getCrowds(film);
            if (crowds != null && !crowds.getList().isEmpty()) {
                this.bbs_crowd$preloadFilmId = film.getId();
                CrowdExportPreload.begin(this.bbs_crowd$preloadFilmId);
            }
        }
    }

    @Override
    protected boolean isWarmupReady() {
        if (this.bbs_crowd$preloadFilmId != null && CrowdSettings.isCrowdExportFull()) {
            if (!CrowdExportPreload.isReady(this.bbs_crowd$preloadFilmId)) {
                return false;
            }
        }
        return super.isWarmupReady();
    }

    @Inject(method = "teardown", at = @At("HEAD"))
    private void bbs_crowd$onTeardown(boolean cancelled, CallbackInfo ci) {
        if (this.bbs_crowd$preloadFilmId != null) {
            CrowdExportPreload.finish(this.bbs_crowd$preloadFilmId);
            this.bbs_crowd$preloadFilmId = null;
        }
    }
}

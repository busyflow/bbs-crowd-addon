package mchorse.bbs_crowd.mixin;

import mchorse.bbs_mod.actions.types.crowd.CrowdExportPreload;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.WorldVideoExportSession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorldVideoExportSession.class, remap = false)
public class WorldVideoExportSessionCrowdMixin {
    @Shadow
    private String filmId;
    @Shadow
    private boolean firstTickPaused;

    @Inject(method = "start", at = @At("HEAD"))
    private void bbs_crowd$onStart(String filmId, Film film, CallbackInfoReturnable<Boolean> cir) {
        if (filmId != null && film != null && mchorse.bbs_crowd.CrowdSettings.isCrowdExportFull()) {
            mchorse.bbs_mod.film.crowds.Crowds crowds = mchorse.bbs_crowd.access.FilmCrowdAccess.getCrowds(film);
            if (crowds != null && !crowds.getList().isEmpty()) {
                CrowdExportPreload.begin(filmId);
            }
        }
    }

    @Inject(method = "isWarmupReady", at = @At("HEAD"), cancellable = true)
    private void bbs_crowd$onIsWarmupReady(CallbackInfoReturnable<Boolean> cir) {
        if (this.filmId != null && this.firstTickPaused && mchorse.bbs_crowd.CrowdSettings.isCrowdExportFull()) {
            if (!CrowdExportPreload.isReady(this.filmId)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "teardown", at = @At("HEAD"))
    private void bbs_crowd$onTeardown(boolean cancelled, CallbackInfo ci) {
        if (this.filmId != null) {
            CrowdExportPreload.finish(this.filmId);
        }
    }
}

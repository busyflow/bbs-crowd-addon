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
        if (filmId != null) {
            CrowdExportPreload.begin(filmId);
        }
    }

    @Inject(method = "isWarmupReady", at = @At("HEAD"), cancellable = true)
    private void bbs_crowd$onIsWarmupReady(CallbackInfoReturnable<Boolean> cir) {
        if (this.filmId != null && this.firstTickPaused) {
            cir.setReturnValue(CrowdExportPreload.isReady(this.filmId));
        }
    }

    @Inject(method = "teardown", at = @At("HEAD"))
    private void bbs_crowd$onTeardown(boolean cancelled, CallbackInfo ci) {
        if (this.filmId != null) {
            CrowdExportPreload.finish(this.filmId);
        }
    }
}

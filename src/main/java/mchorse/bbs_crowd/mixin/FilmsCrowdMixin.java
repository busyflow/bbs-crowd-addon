package mchorse.bbs_crowd.mixin;

import mchorse.bbs_mod.actions.types.crowd.CrowdClientMembers;
import mchorse.bbs_mod.film.Films;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Films.class, remap = false)
public class FilmsCrowdMixin {
    @Inject(method = "stopFilm", at = @At("HEAD"))
    private static void bbs_crowd$onStopFilm(String filmId, CallbackInfo ci) {
        CrowdClientMembers.clear();
    }
}

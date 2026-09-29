package mchorse.bbs_crowd.mixin;

import mchorse.bbs_crowd.access.FilmCrowdAccess;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.crowds.Crowds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Film.class, remap = false)
public class FilmCrowdMixin implements FilmCrowdAccess {
    @Unique
    private Crowds bbs_crowd$crowds;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bbs_crowd$init(String id, CallbackInfo ci) {
        this.bbs_crowd$crowds = new Crowds("crowds");
        ((Film) (Object) this).add(this.bbs_crowd$crowds);
    }

    @Override
    public Crowds bbs_crowd$getCrowds() {
        return this.bbs_crowd$crowds;
    }
}

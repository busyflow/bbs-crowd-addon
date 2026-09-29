package mchorse.bbs_crowd.mixin;

import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.FilmManager;
import mchorse.bbs_mod.film.crowds.CrowdMigration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FilmManager.class, remap = false)
public class FilmManagerCrowdMixin {
    @Inject(method = "createData", at = @At("RETURN"))
    private void bbs_crowd$migrateCrowds(String id, MapType mapType, CallbackInfoReturnable<Film> cir) {
        Film film = cir.getReturnValue();
        if (film != null && mapType != null) {
            CrowdMigration.migrate(film);
        }
    }
}

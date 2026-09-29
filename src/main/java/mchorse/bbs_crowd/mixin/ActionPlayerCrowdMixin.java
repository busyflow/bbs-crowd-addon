package mchorse.bbs_crowd.mixin;

import mchorse.bbs_crowd.network.CrowdServerNetwork;
import mchorse.bbs_mod.actions.ActionPlayer;
import mchorse.bbs_mod.actions.types.crowd.CrowdUtils;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.FilmExportState;
import mchorse.bbs_mod.film.crowds.CrowdKeyframeRuntime;
import mchorse.bbs_mod.film.crowds.CrowdReconciler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = ActionPlayer.class, remap = false)
public class ActionPlayerCrowdMixin {
    @Shadow
    public Film film;
    @Shadow
    public int tick;
    @Shadow
    private ServerWorld world;
    @Shadow
    private ServerPlayerEntity serverPlayer;
    @Shadow
    private Map<String, LivingEntity> actors;

    @Unique
    private final CrowdReconciler bbs_crowd$crowds = new CrowdReconciler();
    @Unique
    private boolean bbs_crowd$crowdExportReadySent;

    @Inject(method = "applyAction", at = @At("HEAD"))
    private void bbs_crowd$applyCrowds(CallbackInfo ci) {
        if (this.film == null || this.world == null) {
            return;
        }

        this.bbs_crowd$crowds.reconcile(this.world, this.film, this.tick);
        CrowdKeyframeRuntime.apply(this.world, this.film, this.tick, this.actors);

        if (!this.bbs_crowd$crowdExportReadySent && this.serverPlayer != null && FilmExportState.isExporting(this.serverPlayer.getUuid())) {
            CrowdServerNetwork.sendCrowdPreloadReady(this.serverPlayer, this.film.getId());
            this.bbs_crowd$crowdExportReadySent = true;
        }
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void bbs_crowd$onStop(CallbackInfo ci) {
        if (this.world != null && this.film != null) {
            CrowdUtils.removeAllForFilm(this.world, this.film);
            this.bbs_crowd$crowds.forget();
        }
    }
}

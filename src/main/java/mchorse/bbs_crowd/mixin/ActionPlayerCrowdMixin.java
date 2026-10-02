package mchorse.bbs_crowd.mixin;

import mchorse.bbs_crowd.network.CrowdServerNetwork;
import mchorse.bbs_mod.actions.ActionPlayer;
import mchorse.bbs_mod.actions.types.crowd.CrowdUtils;
import mchorse.bbs_mod.utils.DataPath;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.FilmExportState;
import mchorse.bbs_mod.film.crowds.Crowd;
import mchorse.bbs_mod.film.crowds.CrowdKeyframeRuntime;
import mchorse.bbs_mod.film.crowds.CrowdReconciler;
import mchorse.bbs_mod.film.crowds.Crowds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private void bbs_crowd$reconcileNow() {
        if (this.film == null || this.world == null) {
            return;
        }

        mchorse.bbs_crowd.CrowdActorContext.currentActors = this.actors;
        mchorse.bbs_crowd.CrowdActorContext.currentRecordingPlayer = this.serverPlayer;
        this.bbs_crowd$crowds.reconcile(this.world, this.film, this.tick);
        CrowdKeyframeRuntime.apply(this.world, this.film, this.tick, this.actors);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void bbs_crowd$onTick(CallbackInfoReturnable<Boolean> cir) {
        this.bbs_crowd$reconcileNow();
    }

    @Inject(method = "applyAction", at = @At("HEAD"))
    private void bbs_crowd$applyCrowds(CallbackInfo ci) {
        this.bbs_crowd$reconcileNow();
    }

    @Inject(method = "updateReplayEntities", at = @At("RETURN"))
    private void bbs_crowd$onUpdateReplayEntities(CallbackInfo ci) {
        this.bbs_crowd$reconcileNow();
    }

    @Inject(method = "resetActorsForRestart", at = @At("HEAD"))
    private void bbs_crowd$onResetActorsHead(CallbackInfo ci) {
        this.bbs_crowd$crowds.forget();
    }

    @Inject(method = "resetActorsForRestart", at = @At("RETURN"))
    private void bbs_crowd$onResetActors(CallbackInfo ci) {
        this.bbs_crowd$reconcileNow();
        if (this.serverPlayer != null && this.film != null) {
            CrowdServerNetwork.sendCrowdPreloadReady(this.serverPlayer, this.film.getId());
        }
    }

    @Inject(method = "goTo(II)V", at = @At("RETURN"))
    private void bbs_crowd$onGoTo(int from, int tick, CallbackInfo ci) {
        this.bbs_crowd$reconcileNow();
    }

    @Inject(method = "syncData", at = @At("HEAD"))
    private void bbs_crowd$onSyncDataHead(DataPath key, BaseType data, CallbackInfo ci) {
        if (this.film != null && key != null && key.size() >= 2 && "crowds".equals(key.strings.get(0))) {
            try {
                int index = Integer.parseInt(key.strings.get(1));
                Crowds crowds = mchorse.bbs_crowd.access.FilmCrowdAccess.getCrowds(this.film);
                if (crowds != null) {
                    while (crowds.getList().size() <= index) {
                        crowds.add(new Crowd(String.valueOf(crowds.getList().size())));
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Inject(method = "syncData", at = @At("RETURN"))
    private void bbs_crowd$onSyncDataReturn(DataPath key, BaseType data, CallbackInfo ci) {
        this.bbs_crowd$reconcileNow();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void bbs_crowd$onStop(CallbackInfo ci) {
        if (this.world != null && this.film != null) {
            CrowdUtils.removeAllForFilm(this.world, this.film);
            this.bbs_crowd$crowds.forget();
        }
    }
}

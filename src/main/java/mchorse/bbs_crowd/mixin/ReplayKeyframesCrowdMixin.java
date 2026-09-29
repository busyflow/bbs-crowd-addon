package mchorse.bbs_crowd.mixin;

import mchorse.bbs_crowd.access.ReplayCrowdChannels;
import mchorse.bbs_crowd.access.ReplayKeyframesCrowdAccess;
import mchorse.bbs_mod.film.replays.ReplayKeyframes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ReplayKeyframes.class, remap = false)
public class ReplayKeyframesCrowdMixin implements ReplayKeyframesCrowdAccess {
    @Unique
    private ReplayCrowdChannels bbs_crowd$channels;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bbs_crowd$init(String id, CallbackInfo ci) {
        this.bbs_crowd$channels = new ReplayCrowdChannels();
        this.bbs_crowd$channels.addTo((ReplayKeyframes) (Object) this);
    }

    @Override
    public ReplayCrowdChannels bbs_crowd$getCrowdChannels() {
        return this.bbs_crowd$channels;
    }
}

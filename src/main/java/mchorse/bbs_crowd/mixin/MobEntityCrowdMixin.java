package mchorse.bbs_crowd.mixin;

import mchorse.bbs_mod.actions.types.crowd.CrowdUtils;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MobEntityCrowdMixin {
    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void bbs_crowd$cancelDespawn(CallbackInfo ci) {
        if (((MobEntity) (Object) this).getCommandTags().contains(CrowdUtils.INTERNAL_TAG)) {
            ci.cancel();
        }
    }

    @Inject(method = "isDisallowedInPeaceful", at = @At("HEAD"), cancellable = true)
    private void bbs_crowd$isDisallowedInPeaceful(CallbackInfoReturnable<Boolean> cir) {
        if (((MobEntity) (Object) this).getCommandTags().contains(CrowdUtils.INTERNAL_TAG)) {
            cir.setReturnValue(false);
        }
    }
}

package mchorse.bbs_crowd.mixin;

import mchorse.bbs_mod.actions.types.crowd.CrowdUtils;
import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HostileEntity.class)
public class HostileEntityCrowdMixin {
    @Inject(method = "isDisallowedInPeaceful", at = @At("HEAD"), cancellable = true)
    private void bbs_crowd$isDisallowedInPeaceful(CallbackInfoReturnable<Boolean> cir) {
        if (((HostileEntity) (Object) this).getCommandTags().contains(CrowdUtils.INTERNAL_TAG)) {
            cir.setReturnValue(false);
        }
    }
}

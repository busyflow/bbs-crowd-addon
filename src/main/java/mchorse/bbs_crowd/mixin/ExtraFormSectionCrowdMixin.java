package mchorse.bbs_crowd.mixin;

import mchorse.bbs_mod.forms.categories.FormCategory;
import mchorse.bbs_mod.forms.forms.CrowdForm;
import mchorse.bbs_mod.forms.sections.ExtraFormSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ExtraFormSection.class, remap = false)
public class ExtraFormSectionCrowdMixin {
    @Shadow
    private FormCategory extra;

    @Inject(method = "initiate", at = @At("RETURN"))
    private void bbs_crowd$addCrowdForm(CallbackInfo ci) {
        if (this.extra != null) {
            this.extra.addForm(new CrowdForm());
        }
    }
}

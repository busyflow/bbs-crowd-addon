package mchorse.bbs_crowd.mixin;

import mchorse.bbs_crowd.CrowdSettings;
import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.settings.SettingsBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BBSSettings.class, remap = false)
public class BBSSettingsCrowdMixin {
    @Inject(method = "register", at = @At("RETURN"))
    private static void bbs_crowd$registerCrowdSettings(SettingsBuilder builder, CallbackInfo ci) {
        CrowdSettings.register(builder);
    }
}

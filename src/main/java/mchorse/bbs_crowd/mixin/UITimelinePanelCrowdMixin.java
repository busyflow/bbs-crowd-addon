package mchorse.bbs_crowd.mixin;

import mchorse.bbs_mod.ui.film.crowds.UICrowdReplayProperties;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.IUIElement;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.utils.UITimelinePanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = UITimelinePanel.class, remap = false)
public class UITimelinePanelCrowdMixin {
    @Shadow
    protected UIElement emptyStateHost;

    @Inject(method = "renderEmptyState", at = @At("HEAD"), cancellable = true)
    private void bbs_crowd$cancelEmptyStateIfCrowdShown(UIContext context, CallbackInfo ci) {
        if (this.emptyStateHost != null) {
            for (IUIElement child : this.emptyStateHost.getChildren()) {
                if (child instanceof UICrowdReplayProperties properties && properties.isVisible()) {
                    ci.cancel();
                    return;
                }
            }
        }
    }
}

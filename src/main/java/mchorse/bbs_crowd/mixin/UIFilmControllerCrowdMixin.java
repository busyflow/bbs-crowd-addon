package mchorse.bbs_crowd.mixin;

import mchorse.bbs_crowd.access.ReplayCrowdChannels;
import mchorse.bbs_mod.actions.crowd.CrowdWalk;
import mchorse.bbs_mod.camera.Camera;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.forms.forms.CrowdForm;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.controller.UIFilmController;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.utils.Area;
import mchorse.bbs_mod.utils.keyframes.Keyframe;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = UIFilmController.class, remap = false)
public class UIFilmControllerCrowdMixin {
    @Shadow
    public UIFilmPanel panel;

    @Inject(
        method = "subMouseClicked",
        at = @At(
            value = "INVOKE",
            target = "Lmchorse/bbs_mod/ui/film/controller/FilmStencilPicker;getHoveredReplayIndex()I"
        ),
        cancellable = true
    )
    private void bbs_crowd$onPickWalkPoint(UIContext context, CallbackInfoReturnable<Boolean> cir) {
        if (context.mouseButton == 0) {
            Keyframe<CrowdWalk> waypoint = this.bbs_crowd$pickWalkPoint(context);
            if (waypoint != null && this.panel != null && this.panel.replayEditor != null
                && this.panel.replayEditor.keyframeEditor != null
                && this.panel.replayEditor.keyframeEditor.view != null) {
                this.panel.replayEditor.keyframeEditor.view.pickKeyframe(waypoint);
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private Keyframe<CrowdWalk> bbs_crowd$pickWalkPoint(UIContext context) {
        if (this.panel == null || this.panel.replayEditor == null) {
            return null;
        }

        Replay replay = this.panel.replayEditor.getReplay();
        if (replay == null || !(replay.form.get() instanceof CrowdForm)) {
            return null;
        }

        ReplayCrowdChannels channels = ReplayCrowdChannels.of(replay.keyframes);
        if (channels == null || channels.crowdWalk.isEmpty()) {
            return null;
        }

        Camera camera = this.panel.getCamera();
        Area viewport = this.panel.preview != null ? this.panel.preview.getViewport() : null;
        if (camera == null || viewport == null) {
            return null;
        }

        Vector3f rayOffset = new Vector3f();
        Vector3f direction = camera.getMouseRay(context.mouseX, context.mouseY, viewport.x, viewport.y, viewport.w, viewport.h, rayOffset);
        Vector3d origin = new Vector3d(camera.position).add(rayOffset.x, rayOffset.y, rayOffset.z);
        Vector3d dir = new Vector3d(direction.x, direction.y, direction.z).normalize();

        Keyframe<CrowdWalk> best = null;
        double bestDistance = Double.MAX_VALUE;

        for (Keyframe<CrowdWalk> keyframe : channels.crowdWalk.getKeyframes()) {
            CrowdWalk walk = keyframe.getValue();
            if (walk == null || !walk.showPoint) {
                continue;
            }

            for (int i = 0; i <= 6; i++) {
                double sampleY = walk.y + 3.0D * (i / 6.0D);
                Vector3d toPoint = new Vector3d(walk.x, sampleY, walk.z).sub(origin);
                double along = toPoint.dot(dir);
                if (along <= 0D) {
                    continue;
                }

                double away = new Vector3d(dir).mul(along).sub(toPoint).length();
                if (away > 0.25D + along * 0.02D) {
                    continue;
                }

                if (along < bestDistance) {
                    bestDistance = along;
                    best = keyframe;
                }
            }
        }

        return best;
    }
}

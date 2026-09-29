package mchorse.bbs_crowd.mixin;

import mchorse.bbs_crowd.access.FilmCrowdAccess;
import mchorse.bbs_crowd.access.ReplayCrowdChannels;
import mchorse.bbs_mod.actions.crowd.CrowdPaint;
import mchorse.bbs_mod.actions.crowd.CrowdWalk;
import mchorse.bbs_mod.actions.types.crowd.CrowdUtils;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.crowds.Crowd;
import mchorse.bbs_mod.film.crowds.Crowds;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.film.replays.tracks.TrackStyle;
import mchorse.bbs_mod.forms.forms.CrowdForm;
import mchorse.bbs_mod.settings.values.base.BaseValue;
import mchorse.bbs_mod.ui.film.UIClipsPanel;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.crowds.UICrowdReplayProperties;
import mchorse.bbs_mod.ui.film.replays.UIReplaysEditor;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UIKeyframeEditor;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UIKeyframeSheet;
import mchorse.bbs_mod.utils.keyframes.KeyframeChannel;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = UIReplaysEditor.class, remap = false)
public class UIReplaysEditorCrowdMixin {
    @Shadow
    public Film film;
    @Shadow
    private Replay replay;
    @Shadow
    private UIFilmPanel filmPanel;
    @Shadow
    private boolean propertiesVisible;
    @Shadow
    private boolean actionsMode;
    @Shadow
    private UIClipsPanel actionTimeline;
    @Shadow
    public UIKeyframeEditor keyframeEditor;

    @Unique
    private UICrowdReplayProperties bbs_crowd$crowdProperties;
    @Unique
    private boolean bbs_crowd$crowdIsShown;

    @Inject(method = "setReplay", at = @At("RETURN"))
    private void bbs_crowd$onSetReplay(Replay replay, CallbackInfo ci) {
        this.bbs_crowd$updateCrowdProperties(replay);
    }

    @Inject(method = "collectCuratedSheets", at = @At("HEAD"), cancellable = true)
    private void bbs_crowd$onCollectCuratedSheets(List<UIKeyframeSheet> sheets, CallbackInfo ci) {
        if (this.replay != null && this.replay.form.get() instanceof CrowdForm) {
            this.bbs_crowd$collectCrowdSheets(sheets);
            ci.cancel();
        }
    }

    @Inject(method = "updateTimelineModeVisibility", at = @At("RETURN"))
    private void bbs_crowd$onUpdateTimelineModeVisibility(CallbackInfo ci) {
        this.bbs_crowd$updateCrowdPropertiesVisibility();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void bbs_crowd$onRender(UIContext context, CallbackInfo ci) {
        this.bbs_crowd$updateCrowdPropertiesVisibility();
    }

    @Unique
    private void bbs_crowd$collectCrowdSheets(List<UIKeyframeSheet> sheets) {
        for (String key : ReplayCrowdChannels.CROWD_CHANNELS) {
            BaseValue value = this.replay.keyframes.get(key);
            if (!(value instanceof KeyframeChannel<?> channel)) {
                continue;
            }

            UIKeyframeSheet sheet = new UIKeyframeSheet(TrackStyle.color(key), channel, null).icon(TrackStyle.icon(key));

            if ("crowd_motion_path".equals(key)) {
                sheet.seed(() -> {
                    CrowdWalk walk = new CrowdWalk();
                    Vec3d center = CrowdUtils.getCrowdCenter(this.film, this.replay, null, this.filmPanel.getCursor());

                    walk.x = (float) center.x;
                    walk.y = (float) center.y;
                    walk.z = (float) center.z;

                    return walk;
                });
            } else if ("crowd_paint".equals(key)) {
                sheet.seed(() -> {
                    Crowds crowds = FilmCrowdAccess.getCrowds(this.film);
                    Crowd crowd = (this.replay.form.get() instanceof CrowdForm cf && crowds != null) ? crowds.byTag(cf.crowd.get()) : null;

                    return new CrowdPaint(crowd == null ? null : crowd.getCells());
                });
            } else if ("crowd_visible".equals(key)) {
                sheet.seed(() -> {
                    ReplayCrowdChannels crowdChannels = ReplayCrowdChannels.of(this.replay.keyframes);
                    return crowdChannels == null || !crowdChannels.crowdVisible.interpolate(this.filmPanel.getCursor(), true);
                });
            }

            sheets.add(sheet);
        }
    }

    @Unique
    private void bbs_crowd$updateCrowdProperties(Replay replay) {
        if (this.bbs_crowd$crowdProperties == null && this.filmPanel != null && this.filmPanel.editArea != null) {
            this.bbs_crowd$crowdProperties = new UICrowdReplayProperties(
                this.filmPanel,
                () -> this.filmPanel.getUndoHandler().getUndoManager().markLastUndoNoMerging()
            );
            this.bbs_crowd$crowdProperties.relative(this.filmPanel.editArea).full(this.filmPanel.editArea);
            this.filmPanel.editArea.add(this.bbs_crowd$crowdProperties);
        }

        if (this.bbs_crowd$crowdProperties != null) {
            this.bbs_crowd$crowdIsShown = this.bbs_crowd$crowdProperties.setReplay(replay);
        }
    }

    @Unique
    private void bbs_crowd$updateCrowdPropertiesVisibility() {
        if (this.bbs_crowd$crowdProperties == null) {
            return;
        }

        boolean occupied = this.actionsMode
            ? this.actionTimeline != null && this.actionTimeline.getClip() != null
            : this.keyframeEditor != null && this.keyframeEditor.editor != null;

        this.bbs_crowd$crowdProperties.setVisible(this.propertiesVisible && this.bbs_crowd$crowdIsShown && !occupied);
    }
}

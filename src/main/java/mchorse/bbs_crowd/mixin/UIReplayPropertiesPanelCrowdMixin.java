package mchorse.bbs_crowd.mixin;

import mchorse.bbs_crowd.access.FilmCrowdAccess;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.crowds.Crowd;
import mchorse.bbs_mod.film.crowds.Crowds;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.forms.forms.CrowdForm;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.network.ClientNetwork;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.crowds.UICrowdSettings;
import mchorse.bbs_mod.ui.film.replays.UIReplayPropertiesPanel;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.UISection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = UIReplayPropertiesPanel.class, remap = false)
public class UIReplayPropertiesPanelCrowdMixin {
    @Shadow
    private UIFilmPanel filmPanel;
    @Shadow
    public UIElement properties;
    @Shadow
    private Replay replay;

    @Unique
    private UISection bbs_crowd$crowdSection;
    @Unique
    private UICrowdSettings bbs_crowd$crowdSettings;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bbs_crowd$onInit(UIFilmPanel filmPanel, CallbackInfo ci) {
        this.bbs_crowd$crowdSettings = new UICrowdSettings(() -> {
            if (this.filmPanel != null && this.filmPanel.getUndoHandler() != null) {
                this.filmPanel.getUndoHandler().getUndoManager().markLastUndoNoMerging();
            }
        });

        this.bbs_crowd$crowdSection = new UISection(IKey.constant("Crowd Options"));
        this.bbs_crowd$crowdSection.fields.add(this.bbs_crowd$crowdSettings);
        this.bbs_crowd$crowdSection.setExpanded(true);
        this.bbs_crowd$crowdSection.setVisible(false);

        if (this.properties != null) {
            this.properties.add(this.bbs_crowd$crowdSection);
        }
    }

    @Inject(method = "setReplay", at = @At("RETURN"))
    private void bbs_crowd$onSetReplay(Replay replay, CallbackInfo ci) {
        if (this.bbs_crowd$crowdSection == null || this.bbs_crowd$crowdSettings == null) {
            return;
        }

        if (replay != null && replay.form.get() instanceof CrowdForm crowdForm) {
            Film film = FilmCrowdAccess.getEditedFilm();
            Crowds crowds = FilmCrowdAccess.getCrowds(film);
            Crowd crowd = (crowds == null) ? null : crowds.byTag(crowdForm.crowd.get());

            if (crowd == null && film != null && crowds != null) {
                crowd = crowds.addCrowd();
                crowd.name.set("Crowd " + crowds.getList().size());
                crowdForm.crowd.set(crowd.crowdTag.get());
                ClientNetwork.sendSyncData(film.getId(), crowds);
                ClientNetwork.sendSyncData(film.getId(), replay.form);
            }

            this.bbs_crowd$crowdSettings.setCrowd(crowd);
            this.bbs_crowd$crowdSection.setVisible(crowd != null);
        } else {
            this.bbs_crowd$crowdSettings.setCrowd(null);
            this.bbs_crowd$crowdSection.setVisible(false);
        }

        if (this.properties != null) {
            this.properties.resize();
        }
    }
}

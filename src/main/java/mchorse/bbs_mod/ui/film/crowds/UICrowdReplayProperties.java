package mchorse.bbs_mod.ui.film.crowds;

import mchorse.bbs_mod.actions.crowd.CrowdPaint;
import mchorse.bbs_mod.actions.types.crowd.CrowdFormation;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.crowds.Crowd;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.forms.forms.CrowdForm;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.clips.area.AreaBrush;
import mchorse.bbs_mod.ui.framework.elements.UIScrollView;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.actions.ActionState;
import mchorse.bbs_mod.network.ClientNetwork;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.utils.ScrollDirection;
import mchorse.bbs_mod.ui.utils.UIConstants;

/**
 * A crowd replay's whole editing surface, in the parameters area beside the timeline.
 *
 * <p>Everything about the crowd is reachable without leaving the shot: the crowd's own settings
 * and its paint brush. It used to take opening the form editor to reach any of it, which is
 * several steps away from the timeline the crowd is being animated on.</p>
 *
 * <p>Only shown while a crowd form is the replay's form, since there is nothing here that means
 * anything otherwise.</p>
 *
 * <p>There is no picker for which crowd this drives, because a crowd replay owns exactly one:
 * giving it a crowd form gives it a crowd. Letting a replay be repointed at any crowd in the film
 * meant one crowd could be driven from two timelines at once, or from a timeline that had nothing
 * to do with it, and neither was ever the intent - two crowds are two replays.</p>
 */
public class UICrowdReplayProperties extends UIScrollView
{
    private final UIFilmPanel filmPanel;
    private final UIToggle keepBehavior;
    private final UICrowdSettings settings;
    private final Runnable onEdit;

    private CrowdForm form;
    private Replay replay;

    public UICrowdReplayProperties(UIFilmPanel filmPanel, Runnable onEdit)
    {
        super(ScrollDirection.VERTICAL);

        this.filmPanel = filmPanel;
        this.onEdit = onEdit;
        this.scroll.cancelScrolling();

        this.keepBehavior = new UIToggle(IKey.constant("Keep behaviour clips"), (b) ->
        {
            if (this.form != null)
            {
                this.form.keepBehavior.set(b.getValue());
            }
        });
        this.keepBehavior.tooltip(IKey.constant("On, keyframes override the crowd's behaviour clips only where they have keys, so a stretch with no walk keyframes leaves the behaviour steering.\n\nOff, the keyframes are the whole story."));
        this.settings = new UICrowdSettings(onEdit);

        this.column(UIConstants.MARGIN).scroll().vertical().stretch().padding(UIConstants.SCROLL_PADDING);

        this.add(this.keepBehavior);
        this.add(this.settings.marginTop(UIConstants.SECTION_GAP));
    }

    /**
     * @return whether this replay has anything for the panel to show, so the caller knows
     *         whether to give it the area at all.
     */
    public boolean setReplay(Replay replay)
    {
        this.replay = replay;
        this.form = replay != null && replay.form.get() instanceof CrowdForm crowdForm ? crowdForm : null;

        if (this.form == null)
        {
            this.settings.setCrowd(null);
            CrowdSelection.set(null);
            AreaBrush.onFinishStroke = null;

            return false;
        }

        AreaBrush.onFinishStroke = this::recordPaintKeyframe;

        this.keepBehavior.setValue(this.form.keepBehavior.get());
        this.bindCrowd();
        this.refresh();

        Film film = mchorse.bbs_crowd.access.FilmCrowdAccess.getEditedFilm();
        if (film != null)
        {
            mchorse.bbs_mod.film.crowds.Crowds crowds = mchorse.bbs_crowd.access.FilmCrowdAccess.getCrowds(film);
            if (crowds != null)
            {
                ClientNetwork.sendSyncData(film.getId(), crowds);
            }
            if (this.replay != null)
            {
                ClientNetwork.sendSyncData(film.getId(), this.replay.form);
            }
            this.filmPanel.notifyServer(ActionState.SEEK);
        }

        /* The settings sub-panel starts collapsed and is flipped visible by refresh() above; without
         * relaying out here its rows keep the zero-height areas they were given while hidden, so only
         * the two rows this panel owns directly (crowd + New crowd) show. It used to take an unrelated
         * resize - selecting then deselecting a keyframe - to fix itself. This lays it out at once. */
        this.resize();

        return true;
    }

    /**
     * Give the replay its own crowd, so the settings below are simply there.
     *
     * <p>Always a fresh one, never the film's first: adopting an existing crowd is what let two
     * replays drive the same people. A form that already names a crowd the film still has keeps
     * it, which is what carries an old film's pairings across unchanged.</p>
     */
    private void bindCrowd()
    {
        Film film = mchorse.bbs_crowd.access.FilmCrowdAccess.getEditedFilm();
        mchorse.bbs_mod.film.crowds.Crowds crowds = mchorse.bbs_crowd.access.FilmCrowdAccess.getCrowds(film);

        if (film == null || crowds == null || crowds.byTag(this.form.crowd.get()) != null)
        {
            return;
        }

        Crowd crowd = crowds.addCrowd();

        crowd.name.set("Crowd " + crowds.getList().size());

        this.form.crowd.set(crowd.crowdTag.get());

        if (this.replay != null)
        {
            this.replay.form.preNotify();
            this.replay.form.postNotify();
            ClientNetwork.sendSyncData(film.getId(), this.replay.form);
        }
        ClientNetwork.sendSyncData(film.getId(), crowds);
    }

    private void refresh()
    {
        Film film = mchorse.bbs_crowd.access.FilmCrowdAccess.getEditedFilm();
        mchorse.bbs_mod.film.crowds.Crowds crowds = mchorse.bbs_crowd.access.FilmCrowdAccess.getCrowds(film);
        Crowd crowd = (film == null || crowds == null) ? null : crowds.byTag(this.form.crowd.get());

        this.settings.setCrowd(crowd);

        /* The viewport draws the painted outline and the formation ring for whichever crowd is
         * being edited, and this is now the usual place to edit one. */
        if (crowd != null)
        {
            CrowdSelection.set(crowd);
        }
    }

    public void recordPaintKeyframe()
    {
        if (this.replay == null || this.settings.getCrowd() == null)
        {
            return;
        }

        Crowd crowd = this.settings.getCrowd();

        if (crowd.getFormation() != CrowdFormation.PAINT)
        {
            return;
        }

        int cursor = this.filmPanel.getCursor();
        CrowdPaint paint = new CrowdPaint(crowd.getCells());

        mchorse.bbs_crowd.access.ReplayCrowdChannels channels = mchorse.bbs_crowd.access.ReplayCrowdChannels.of(this.replay.keyframes);
        if (channels != null)
        {
            channels.crowdPaint.insert(cursor, paint);
            channels.crowdPaint.postNotify();

            Film film = mchorse.bbs_crowd.access.FilmCrowdAccess.getEditedFilm();
            if (film != null)
            {
                ClientNetwork.sendSyncData(film.getId(), channels.crowdPaint);
                ClientNetwork.sendSyncData(film.getId(), crowd);
                ClientNetwork.sendSyncData(film.getId(), this.replay.form);
            }
        }

        if (this.filmPanel.replayEditor != null)
        {
            this.filmPanel.replayEditor.updateChannelsList();
        }

        if (this.onEdit != null)
        {
            this.onEdit.run();
        }

        this.filmPanel.notifyServer(ActionState.SEEK);
    }

    @Override
    public void render(UIContext context)
    {
        if (this.isVisible())
        {
            this.area.render(context.batcher, BBSSettings.baseSurface());
        }

        super.render(context);
    }
}

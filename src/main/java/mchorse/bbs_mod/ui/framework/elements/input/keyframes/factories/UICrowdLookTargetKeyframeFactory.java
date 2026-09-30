package mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories;

import mchorse.bbs_mod.actions.crowd.CrowdLookTarget;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UIKeyframes;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.UIConstants;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import mchorse.bbs_mod.utils.colors.Colors;
import mchorse.bbs_mod.utils.keyframes.Keyframe;

/**
 * Apple/1UI-grade keyframe editor for Crowd Look Target.
 *
 * <p>Allows picking any scene replay as a gaze target with intuitive, non-redundant controls
 * for head yaw, vertical pitch, and whole-body alignment.</p>
 */
public class UICrowdLookTargetKeyframeFactory extends UIKeyframeFactory<String>
{
    private final UIButton target;
    private final UIButton clearTarget;
    private final UIToggle headYaw;
    private final UIToggle pitch;
    private final UIToggle bodyYaw;

    public UICrowdLookTargetKeyframeFactory(Keyframe<String> keyframe, UIKeyframes editor)
    {
        super(keyframe, editor);

        this.target = new UIButton(IKey.EMPTY, (button) -> this.openTargetPicker());
        this.target.tooltip(IKey.constant("Pick a replay that crowd members look at."));

        this.clearTarget = new UIButton(IKey.constant("X"), (button) -> this.pickTarget(""));
        this.clearTarget.tooltip(IKey.constant("Clear target (look straight ahead)"));
        this.clearTarget.color(Colors.NEGATIVE);

        this.headYaw = new UIToggle(IKey.constant("Head Turn (horizontal)"), b -> this.updateControl(null, b.getValue(), null));
        this.headYaw.tooltip(IKey.constant("Rotate member heads horizontally towards the target."));

        this.pitch = new UIToggle(IKey.constant("Head Tilt (vertical)"), b -> this.updateControl(b.getValue(), null, null));
        this.pitch.tooltip(IKey.constant("Tilt member heads up or down towards the target."));

        this.bodyYaw = new UIToggle(IKey.constant("Turn Body"), b -> this.updateControl(null, null, b.getValue()));
        this.bodyYaw.tooltip(IKey.constant("Turn the member's entire body towards the target, rather than only rotating the head."));

        UIElement targetRow = UI.row(4, this.target, this.clearTarget.w(24));

        UIElement content = UI.column(
            UIConstants.MARGIN,
            UI.label(IKey.constant("Crowd Look Target")),
            UI.label(IKey.constant("Members turn to track the selected replay target in the scene.")),
            targetRow.marginTop(UIConstants.SECTION_GAP),
            this.headYaw,
            this.pitch,
            this.bodyYaw
        );

        this.updateTargetLabel();
        this.updateControls();
        this.scroll.add(content);
    }

    private Film getFilm()
    {
        UIFilmPanel panel = this.getParent(UIFilmPanel.class);

        return panel == null ? null : panel.getData();
    }

    private void updateTargetLabel()
    {
        Film film = this.getFilm();
        String replayId = CrowdLookTarget.parse(this.keyframe.getValue()).replayId();
        Replay replay = film == null ? null : (Replay) film.replays.get(replayId);

        this.target.label = IKey.constant(replay == null ? "Select target replay..." : replay.getName());
    }

    private void openTargetPicker()
    {
        UIContext context = this.getContext();
        Film film = this.getFilm();

        if (context == null || film == null)
        {
            return;
        }

        context.replaceContextMenu((menu) ->
        {
            menu.autoKeys();
            menu.action(Icons.CLOSE, IKey.constant("No target (look ahead)"), Colors.NEGATIVE, () -> this.pickTarget(""));

            for (Replay replay : film.replays.getList())
            {
                String id = replay.getId();
                String label = replay.getName();

                menu.action(Icons.FILM, IKey.constant(label),
                    id.equals(CrowdLookTarget.parse(this.keyframe.getValue()).replayId()), () -> this.pickTarget(id));
            }
        });
    }

    private void pickTarget(String replayId)
    {
        CrowdLookTarget value = CrowdLookTarget.parse(this.keyframe.getValue());

        this.setValue(new CrowdLookTarget(replayId, value.headYaw() || value.bodyYaw(), value.pitch(), value.bodyYaw(), value.headYaw()).encode());
        this.updateTargetLabel();
    }

    private void updateControl(Boolean pitch, Boolean headYaw, Boolean bodyYaw)
    {
        CrowdLookTarget value = CrowdLookTarget.parse(this.keyframe.getValue());

        boolean newHeadYaw = headYaw == null ? value.headYaw() : headYaw;
        boolean newPitch = pitch == null ? value.pitch() : pitch;
        boolean newBodyYaw = bodyYaw == null ? value.bodyYaw() : bodyYaw;
        boolean newYaw = newHeadYaw || newBodyYaw;

        this.setValue(new CrowdLookTarget(
            value.replayId(),
            newYaw,
            newPitch,
            newBodyYaw,
            newHeadYaw
        ).encode());
        this.updateControls();
    }

    private void updateControls()
    {
        CrowdLookTarget value = CrowdLookTarget.parse(this.keyframe.getValue());

        this.headYaw.setValue(value.headYaw());
        this.pitch.setValue(value.pitch());
        this.bodyYaw.setValue(value.bodyYaw());
    }

    @Override
    public void update()
    {
        super.update();
        this.updateTargetLabel();
        this.updateControls();
    }
}

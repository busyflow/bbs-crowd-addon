package mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories;

import mchorse.bbs_mod.actions.crowd.CrowdJump;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UIKeyframes;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.UIConstants;
import mchorse.bbs_mod.utils.keyframes.Keyframe;

import java.util.function.Consumer;

/**
 * Apple/1UI-grade keyframe editor for crowd jumping dynamics.
 */
public class UICrowdJumpKeyframeFactory extends UIKeyframeFactory<CrowdJump>
{
    private final UITrackpad amount;
    private final UITrackpad rate;
    private final UIToggle random;

    public UICrowdJumpKeyframeFactory(Keyframe<CrowdJump> keyframe, UIKeyframes editor)
    {
        super(keyframe, editor);

        if (keyframe.getValue() == null)
        {
            keyframe.setValue(new CrowdJump());
        }

        this.amount = new UITrackpad((value) -> this.edit((jump) -> jump.amount = value.floatValue()));
        this.amount.limit(0D, 1D).increment(0.05D).values(0.05D, 0.01D, 0.2D);
        this.amount.tooltip(IKey.constant("Share of the crowd that participates in jumps (0.0 = none, 1.0 = everyone)."));

        this.rate = new UITrackpad((value) -> this.edit((jump) -> jump.rate = value.floatValue()));
        this.rate.limit(0D, 1D).increment(0.05D).values(0.05D, 0.01D, 0.2D);
        this.rate.tooltip(IKey.constant("Jump frequency: 0.0 is one jump per member; 1.0 jumps immediately upon landing."));

        this.random = new UIToggle(IKey.constant("Organic Variation"), (b) -> this.edit((jump) -> jump.random = b.getValue()));
        this.random.tooltip(IKey.constant("Vary jump height and duration across members so arcs appear natural rather than synchronized."));

        UIElement content = UI.column(
            UIConstants.MARGIN,
            UI.label(IKey.constant("Crowd Jump Dynamics")),
            UI.label(IKey.constant("Configures jumping participation and jump frequencies from this keyframe.")),
            UI.labelRow(IKey.constant("Jumping Share"), this.amount).marginTop(UIConstants.SECTION_GAP),
            UI.labelRow(IKey.constant("Jump Frequency"), this.rate),
            this.random.marginTop(UIConstants.SECTION_GAP)
        );

        this.scroll.add(content);
        this.display();
    }

    private void edit(Consumer<CrowdJump> consumer)
    {
        CrowdJump jump = this.keyframe.getValue();

        if (jump == null)
        {
            jump = new CrowdJump();
            this.keyframe.setValue(jump);
        }

        this.keyframe.preNotify();
        consumer.accept(jump);
        this.keyframe.postNotify();
    }

    private void display()
    {
        CrowdJump jump = this.keyframe.getValue();

        if (jump == null)
        {
            return;
        }

        this.amount.setValue(jump.amount);
        this.rate.setValue(jump.rate);
        this.random.setValue(jump.random);
    }
}

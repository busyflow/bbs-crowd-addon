package mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories;

import mchorse.bbs_mod.actions.crowd.CrowdPaint;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UIKeyframes;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UITrackValue;
import mchorse.bbs_mod.ui.framework.elements.utils.UILabel;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.UIConstants;
import mchorse.bbs_mod.utils.colors.Colors;

import java.util.function.Consumer;

/**
 * Apple/1UI-grade keyframe editor for painted crowd ground formations.
 */
public class UICrowdPaintKeyframeFactory extends UIKeyframeFactory<CrowdPaint>
{
    private final UILabel blockCount;
    private final UITrackpad stagger;
    private final UITrackpad spread;
    private final UIToggle terrain;
    private final UIToggle run;
    private final UIButton clear;

    public UICrowdPaintKeyframeFactory(UITrackValue<CrowdPaint> track, UIKeyframes editor)
    {
        super(track, editor);

        if (track.getValue() == null)
        {
            track.setValue(new CrowdPaint());
        }

        this.blockCount = UI.label(IKey.constant("0 blocks painted"));

        this.stagger = new UITrackpad((value) -> this.edit((p) -> p.stagger = value.floatValue()));
        this.stagger.limit(0D, 1D).increment(0.05D).values(0.05D, 0.01D, 0.2D);
        this.stagger.tooltip(IKey.constant("Departure stagger: how ragged the crowd is about moving into this painted formation."));

        this.spread = new UITrackpad((value) -> this.edit((p) -> p.spread = value.floatValue()));
        this.spread.limit(0D, 1D).increment(0.05D).values(0.05D, 0.01D, 0.2D);
        this.spread.tooltip(IKey.constant("Formation expansion / breathing room while traveling into this shape."));

        this.terrain = new UIToggle(IKey.constant("Follow Terrain Contours"), (b) -> this.edit((p) -> p.terrainFollow = b.getValue()));
        this.terrain.tooltip(IKey.constant("Adhere member elevations to uphill/downhill surface contours."));

        this.run = new UIToggle(IKey.constant("Run (Sprint)"), (b) -> this.edit((p) -> p.run = b.getValue()));
        this.run.tooltip(IKey.constant("Sprint into position towards this painted formation."));

        this.clear = new UIButton(IKey.constant("Clear Painted Blocks"), (b) ->
        {
            this.edit((p) -> p.getCells().clear());
        });
        this.clear.tooltip(IKey.constant("Remove all painted blocks stored on this keyframe."));
        this.clear.color(Colors.NEGATIVE);

        UIElement content = UI.column(
            UIConstants.MARGIN,
            UI.label(IKey.constant("Crowd Painted Formation")),
            UI.label(IKey.constant("Keyframe formation defined by hand-painted terrain cells.")),
            this.blockCount.marginTop(UIConstants.SECTION_GAP),
            UI.labelRow(IKey.constant("Transition Stagger"), this.stagger),
            UI.labelRow(IKey.constant("Transit Spread"), this.spread),
            UI.row(4, this.terrain, this.run).marginTop(UIConstants.SECTION_GAP),
            this.clear.marginTop(UIConstants.SECTION_GAP)
        );

        this.scroll.add(content);
        this.display();
    }

    private void edit(Consumer<CrowdPaint> consumer)
    {
        this.track.edit(consumer);
        this.display();
    }

    private void display()
    {
        CrowdPaint paint = this.track.getValue();

        if (paint != null)
        {
            this.blockCount.label = IKey.constant(paint.size() + " blocks painted");
            this.stagger.setValue(paint.stagger);
            this.spread.setValue(paint.spread);
            this.terrain.setValue(paint.terrainFollow);
            this.run.setValue(paint.run);
        }
    }

    @Override
    public void update()
    {
        super.update();
        this.display();
    }
}

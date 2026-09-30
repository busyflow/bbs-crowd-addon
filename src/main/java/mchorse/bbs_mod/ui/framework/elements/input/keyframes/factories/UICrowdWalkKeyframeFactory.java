package mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories;

import mchorse.bbs_mod.actions.crowd.CrowdWalk;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.settings.values.IValueListener;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UIPropTransform;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UIKeyframes;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.UIConstants;
import mchorse.bbs_mod.utils.keyframes.Keyframe;
import mchorse.bbs_mod.utils.pose.Transform;

/**
 * Apple/1UI-grade keyframe editor for crowd walk waypoints.
 */
public class UICrowdWalkKeyframeFactory extends UIKeyframeFactory<CrowdWalk>
{
    public final UIPropTransform transform = new UIPropTransform();

    private final UITrackpad x;
    private final UITrackpad y;
    private final UITrackpad z;
    private final UITrackpad stagger;
    private final UITrackpad spread;
    private final UIToggle run;
    private final UIToggle faceTravel;
    private final UIToggle terrain;
    private final UIToggle showPath;
    private final UIToggle showPoint;
    private final UIElement content;
    private final Transform edited = new Transform();
    private boolean syncing;

    public UICrowdWalkKeyframeFactory(Keyframe<CrowdWalk> keyframe, UIKeyframes editor)
    {
        super(keyframe, editor);

        if (keyframe.getValue() == null)
        {
            keyframe.setValue(new CrowdWalk());
        }

        keyframe.setDuration(0F);
        this.duration.setVisible(false);

        this.x = trackpad(-10000D, 10000D, 0.25D, v -> this.edit(p -> p.x = v.floatValue()));
        this.x.tooltip(IKey.constant("Waypoint X coordinate in blocks."));
        this.y = trackpad(-10000D, 10000D, 0.25D, v -> this.edit(p -> p.y = v.floatValue()));
        this.y.tooltip(IKey.constant("Waypoint Y coordinate in blocks."));
        this.z = trackpad(-10000D, 10000D, 0.25D, v -> this.edit(p -> p.z = v.floatValue()));
        this.z.tooltip(IKey.constant("Waypoint Z coordinate in blocks."));

        this.stagger = trackpad(0D, 1D, 0.05D, v -> this.edit(p -> p.stagger = v.floatValue()));
        this.stagger.tooltip(IKey.constant("Departure stagger: 0 moves members in unison; 1 spreads start timing across members."));

        this.spread = trackpad(0D, 1D, 0.05D, v -> this.edit(p -> p.spread = v.floatValue()));
        this.spread.tooltip(IKey.constant("Formation expansion during transit (returns to exact formation at destination)."));

        this.faceTravel = new UIToggle(IKey.constant("Face Direction of Travel"), b -> this.edit(p -> p.faceTravel = b.getValue()));
        this.faceTravel.tooltip(IKey.constant("Rotate member headings towards their motion vector."));

        this.terrain = new UIToggle(IKey.constant("Follow Terrain Contours"), b -> this.edit(p -> p.terrainFollow = b.getValue()));
        this.terrain.tooltip(IKey.constant("Adhere member elevations to uphill/downhill surface contours."));

        this.run = new UIToggle(IKey.constant("Run (Sprint)"), b -> this.edit(p -> p.run = b.getValue()));
        this.run.tooltip(IKey.constant("Sprint towards this waypoint at high speed."));

        this.showPath = new UIToggle(IKey.constant("Show Walk Path"), b -> this.edit(p -> p.showPath = b.getValue()));
        this.showPath.tooltip(IKey.constant("Render walk path spline in the 3D viewport."));

        this.showPoint = new UIToggle(IKey.constant("Show Waypoint Marker"), b -> this.edit(p -> p.showPoint = b.getValue()));
        this.showPoint.tooltip(IKey.constant("Render waypoint marker and gizmo in the 3D viewport."));

        this.transform.callbacks(
            () -> this.keyframe.preNotify(),
            () ->
            {
                this.syncFromTransform();
                this.keyframe.postNotify();
                this.display(false);
            },
            () -> this.keyframe.preNotify(IValueListener.FLAG_UNMERGEABLE)
        );
        this.transform.enableTranslateHotkeys();

        this.content = UI.column(
            UIConstants.MARGIN,
            UI.label(IKey.constant("Crowd Walk Waypoint")),
            UI.label(IKey.constant("Members navigate to this destination waypoint over the timeline.")),
            UI.labelRow(IKey.constant("Position (X/Y/Z)"), UI.row(4, this.x, this.y, this.z)).marginTop(UIConstants.SECTION_GAP),
            UI.labelRow(IKey.constant("Departure Stagger"), this.stagger),
            UI.labelRow(IKey.constant("Transit Spread"), this.spread),
            this.faceTravel.marginTop(UIConstants.SECTION_GAP),
            this.terrain,
            this.run,
            this.showPath.marginTop(UIConstants.SECTION_GAP),
            this.showPoint
        );
        this.scroll.add(this.content);

        this.transform.setVisible(false);
        this.add(this.transform);
        this.display(true);
    }

    private UITrackpad trackpad(double min, double max, double increment, java.util.function.Consumer<Double> callback)
    {
        return new UITrackpad(callback).limit(min, max).increment(increment).values(increment, increment * 0.2D, increment * 4D);
    }

    public CrowdWalk getPath()
    {
        return this.keyframe.getValue();
    }

    private void edit(java.util.function.Consumer<CrowdWalk> consumer)
    {
        CrowdWalk point = this.getPath();

        if (point == null)
        {
            point = new CrowdWalk();
            this.keyframe.setValue(point);
        }

        this.keyframe.preNotify();
        consumer.accept(point);
        this.keyframe.postNotify();
        this.display(true);
    }

    private void syncFromTransform()
    {
        CrowdWalk point = this.getPath();
        Transform value = this.transform.getTransform();

        if (this.syncing || point == null || value == null)
        {
            return;
        }

        point.x = value.translate.x;
        point.y = value.translate.y;
        point.z = value.translate.z;
    }

    private void display(boolean transformToo)
    {
        CrowdWalk point = this.getPath();

        if (point == null)
        {
            return;
        }

        this.syncing = true;

        try
        {
            this.x.setValue(point.x);
            this.y.setValue(point.y);
            this.z.setValue(point.z);
            this.stagger.setValue(point.stagger);
            this.spread.setValue(point.spread);
            this.run.setValue(point.run);
            this.faceTravel.setValue(point.faceTravel);
            this.terrain.setValue(point.terrainFollow);
            this.showPath.setValue(point.showPath);
            this.showPoint.setValue(point.showPoint);
            this.content.resize();
            this.scroll.resize();

            if (transformToo)
            {
                this.edited.identity();
                this.edited.translate.set(point.x, point.y, point.z);
                this.transform.setTransform(this.edited);
            }
        }
        finally
        {
            this.syncing = false;
        }
    }

    @Override
    public void update()
    {
        super.update();
        this.display(!this.transform.isEditing());
    }
}

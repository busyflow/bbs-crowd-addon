package mchorse.bbs_crowd.client;

import mchorse.bbs_crowd.BBSCrowdMod;
import mchorse.bbs_crowd.CrowdKeyframeFactories;
import mchorse.bbs_crowd.network.CrowdClientNetwork;
import mchorse.bbs_mod.actions.types.crowd.CrowdBehaviorActionClip;
import mchorse.bbs_mod.film.replays.tracks.TrackStyle;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.forms.CrowdForm;
import mchorse.bbs_mod.forms.renderers.CrowdFormRenderer;
import mchorse.bbs_mod.ui.film.clips.UIClip;
import mchorse.bbs_mod.ui.film.clips.actions.UICrowdBehaviorActionClip;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories.UICrowdBehaviorKeyframeFactory;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories.UICrowdJumpKeyframeFactory;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories.UICrowdLookTargetKeyframeFactory;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories.UICrowdPaintKeyframeFactory;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories.UICrowdTextureKeyframeFactory;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories.UICrowdWalkKeyframeFactory;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories.UIKeyframeFactory;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import mchorse.bbs_mod.utils.colors.Colors;
import net.fabricmc.api.ClientModInitializer;

public class BBSCrowdModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FormUtilsClient.register(CrowdForm.class, CrowdFormRenderer::new);
        mchorse.bbs_mod.ui.forms.editors.UIFormEditor.register(CrowdForm.class, mchorse.bbs_mod.ui.forms.editors.forms.UICrowdForm::new);

        UIClip.register(CrowdBehaviorActionClip.class, UICrowdBehaviorActionClip::new);

        UIKeyframeFactory.register(CrowdKeyframeFactories.CROWD_LOOK_TARGET, UICrowdLookTargetKeyframeFactory::new);
        UIKeyframeFactory.register(CrowdKeyframeFactories.CROWD_JUMP, UICrowdJumpKeyframeFactory::new);
        UIKeyframeFactory.register(CrowdKeyframeFactories.CROWD_WALK, UICrowdWalkKeyframeFactory::new);
        UIKeyframeFactory.register(CrowdKeyframeFactories.CROWD_PAINT, UICrowdPaintKeyframeFactory::new);
        UIKeyframeFactory.register(CrowdKeyframeFactories.CROWD_TEXTURE, UICrowdTextureKeyframeFactory::new);
        UIKeyframeFactory.register(CrowdKeyframeFactories.CROWD_BEHAVIOR, UICrowdBehaviorKeyframeFactory::new);

        TrackStyle.register("crowd_visible", Icons.VISIBLE, Colors.ACTIVE);
        TrackStyle.register("crowd_behavior", Icons.SHAPES, Colors.GREEN);
        TrackStyle.register("crowd_look_target", Icons.VISIBLE, Colors.CYAN);
        TrackStyle.register("crowd_jump", Icons.VERTICAL, Colors.YELLOW);
        TrackStyle.register("crowd_motion_path", Icons.ALL_DIRECTIONS, Colors.MAGENTA);
        TrackStyle.register("crowd_paint", Icons.BLOCK, Colors.PLANE_XY);
        TrackStyle.register("crowd_texture", Icons.MATERIAL, Colors.ORANGE);
        TrackStyle.register("crowd_color", Icons.COLOR, Colors.PINK);

        CrowdClientNetwork.registerClientReceivers();

        BBSCrowdMod.LOGGER.info("Initialized BBS Crowd Addon (Client)");
    }
}

package mchorse.bbs_crowd;

import mchorse.bbs_mod.utils.keyframes.factories.CrowdBehaviorKeyframeFactory;
import mchorse.bbs_mod.utils.keyframes.factories.CrowdJumpKeyframeFactory;
import mchorse.bbs_mod.utils.keyframes.factories.CrowdLookTargetKeyframeFactory;
import mchorse.bbs_mod.utils.keyframes.factories.CrowdPaintKeyframeFactory;
import mchorse.bbs_mod.utils.keyframes.factories.CrowdTextureKeyframeFactory;
import mchorse.bbs_mod.utils.keyframes.factories.CrowdWalkKeyframeFactory;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;

public class CrowdKeyframeFactories {
    public static final CrowdLookTargetKeyframeFactory CROWD_LOOK_TARGET = new CrowdLookTargetKeyframeFactory();
    public static final CrowdJumpKeyframeFactory CROWD_JUMP = new CrowdJumpKeyframeFactory();
    public static final CrowdWalkKeyframeFactory CROWD_WALK = new CrowdWalkKeyframeFactory();
    public static final CrowdTextureKeyframeFactory CROWD_TEXTURE = new CrowdTextureKeyframeFactory();
    public static final CrowdBehaviorKeyframeFactory CROWD_BEHAVIOR = new CrowdBehaviorKeyframeFactory();
    public static final CrowdPaintKeyframeFactory CROWD_PAINT = new CrowdPaintKeyframeFactory();

    public static void register() {
        KeyframeFactories.FACTORIES.put("crowd_look_target", CROWD_LOOK_TARGET);
        KeyframeFactories.FACTORIES.put("crowd_jump", CROWD_JUMP);
        KeyframeFactories.FACTORIES.put("crowd_motion_path", CROWD_WALK);
        KeyframeFactories.FACTORIES.put("crowd_texture", CROWD_TEXTURE);
        KeyframeFactories.FACTORIES.put("crowd_behavior", CROWD_BEHAVIOR);
        KeyframeFactories.FACTORIES.put("crowd_paint", CROWD_PAINT);
    }
}

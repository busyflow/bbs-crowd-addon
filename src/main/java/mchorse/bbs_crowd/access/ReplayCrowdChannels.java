package mchorse.bbs_crowd.access;

import mchorse.bbs_crowd.CrowdKeyframeFactories;
import mchorse.bbs_mod.actions.crowd.CrowdBehavior;
import mchorse.bbs_mod.actions.crowd.CrowdJump;
import mchorse.bbs_mod.actions.crowd.CrowdPaint;
import mchorse.bbs_mod.actions.crowd.CrowdTexture;
import mchorse.bbs_mod.actions.crowd.CrowdWalk;
import mchorse.bbs_mod.film.replays.ReplayKeyframes;
import mchorse.bbs_mod.utils.colors.Color;
import mchorse.bbs_mod.utils.keyframes.KeyframeChannel;
import mchorse.bbs_mod.utils.keyframes.factories.KeyframeFactories;

import java.util.Arrays;
import java.util.List;

public class ReplayCrowdChannels {
    public static final List<String> CROWD_CHANNELS = Arrays.asList(
        "crowd_visible", "crowd_behavior", "crowd_look_target", "crowd_jump",
        "crowd_motion_path", "crowd_paint", "crowd_texture", "crowd_color"
    );

    public final KeyframeChannel<String> crowdLookTarget = new KeyframeChannel<>("crowd_look_target", CrowdKeyframeFactories.CROWD_LOOK_TARGET);
    public final KeyframeChannel<Boolean> crowdVisible = new KeyframeChannel<>("crowd_visible", KeyframeFactories.BOOLEAN);
    public final KeyframeChannel<CrowdBehavior> crowdBehavior = new KeyframeChannel<>("crowd_behavior", CrowdKeyframeFactories.CROWD_BEHAVIOR);
    public final KeyframeChannel<CrowdJump> crowdJump = new KeyframeChannel<>("crowd_jump", CrowdKeyframeFactories.CROWD_JUMP);
    public final KeyframeChannel<CrowdWalk> crowdWalk = new KeyframeChannel<>("crowd_motion_path", CrowdKeyframeFactories.CROWD_WALK);
    public final KeyframeChannel<CrowdPaint> crowdPaint = new KeyframeChannel<>("crowd_paint", CrowdKeyframeFactories.CROWD_PAINT);
    public final KeyframeChannel<CrowdTexture> crowdTexture = new KeyframeChannel<>("crowd_texture", CrowdKeyframeFactories.CROWD_TEXTURE);
    public final KeyframeChannel<Color> crowdColor = new KeyframeChannel<>("crowd_color", KeyframeFactories.COLOR);

    public void addTo(ReplayKeyframes keyframes) {
        keyframes.add(this.crowdVisible);
        keyframes.add(this.crowdLookTarget);
        keyframes.add(this.crowdBehavior);
        keyframes.add(this.crowdJump);
        keyframes.add(this.crowdWalk);
        keyframes.add(this.crowdPaint);
        keyframes.add(this.crowdTexture);
        keyframes.add(this.crowdColor);
    }

    public static ReplayCrowdChannels of(ReplayKeyframes keyframes) {
        if (keyframes instanceof ReplayKeyframesCrowdAccess access) {
            return access.bbs_crowd$getCrowdChannels();
        }
        return null;
    }

    public static ReplayCrowdChannels attach(ReplayKeyframes keyframes) {
        ReplayCrowdChannels existing = of(keyframes);
        if (existing != null) {
            return existing;
        }
        ReplayCrowdChannels channels = new ReplayCrowdChannels();
        channels.addTo(keyframes);
        return channels;
    }
}

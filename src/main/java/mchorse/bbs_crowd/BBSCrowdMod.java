package mchorse.bbs_crowd;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.actions.types.crowd.CrowdBehaviorActionClip;
import mchorse.bbs_mod.actions.types.crowd.CrowdSpawnActionClip;
import mchorse.bbs_mod.camera.clips.ClipFactoryData;
import mchorse.bbs_mod.forms.forms.CrowdForm;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import mchorse.bbs_mod.utils.colors.Colors;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BBSCrowdMod implements ModInitializer {
    public static final String MOD_ID = "bbs_crowd";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CrowdKeyframeFactories.register();

        BBSMod.getForms().register(Link.bbs("crowd"), CrowdForm.class, null);
        BBSMod.getForms().register(new Link(MOD_ID, "crowd"), CrowdForm.class, null);

        BBSMod.getFactoryActionClips().register(
            Link.bbs("crowd_behavior"),
            CrowdBehaviorActionClip.class,
            new ClipFactoryData(Icons.ALL_DIRECTIONS, Colors.CYAN)
        );
        BBSMod.getFactoryActionClips().register(
            new Link(MOD_ID, "crowd_behavior"),
            CrowdBehaviorActionClip.class,
            new ClipFactoryData(Icons.ALL_DIRECTIONS, Colors.CYAN)
        );

        BBSMod.getFactoryActionClips().register(
            Link.bbs("crowd_spawn"),
            CrowdSpawnActionClip.class,
            new ClipFactoryData(Icons.CHICKEN, Colors.GREEN)
        );
        BBSMod.getFactoryActionClips().register(
            new Link(MOD_ID, "crowd_spawn"),
            CrowdSpawnActionClip.class,
            new ClipFactoryData(Icons.CHICKEN, Colors.GREEN)
        );

        LOGGER.info("Initialized BBS Crowd Addon (Common)");
    }
}

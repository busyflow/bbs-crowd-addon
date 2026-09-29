package mchorse.bbs_crowd;

import mchorse.bbs_mod.settings.SettingsBuilder;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class CrowdSettings {
    public static ValueInt crowdPreviewCount;

    public static void register(SettingsBuilder builder) {
        builder.category("crowd", Icons.CHICKEN);
        crowdPreviewCount = builder.getInt("crowd_preview_count", 500, 0, 100000);
    }

    public static int getCrowdPreviewCount() {
        return crowdPreviewCount == null ? 500 : crowdPreviewCount.get();
    }
}

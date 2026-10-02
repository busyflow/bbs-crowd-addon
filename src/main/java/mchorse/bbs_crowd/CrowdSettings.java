package mchorse.bbs_crowd;

import mchorse.bbs_mod.settings.SettingsBuilder;
import mchorse.bbs_mod.settings.values.numeric.ValueBoolean;
import mchorse.bbs_mod.settings.values.numeric.ValueInt;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class CrowdSettings {
    public static ValueInt crowdPreviewCount;
    public static ValueBoolean crowdExportFull;

    public static void register(SettingsBuilder builder) {
        builder.category("crowd", Icons.CHICKEN);
        crowdPreviewCount = builder.getInt("crowd_preview_count", 500, 0, 100000);
        crowdExportFull = builder.getBoolean("crowd_export_full", true);
    }

    public static int getCrowdPreviewCount() {
        return crowdPreviewCount == null ? 500 : crowdPreviewCount.get();
    }

    public static boolean isCrowdExportFull() {
        return crowdExportFull == null || crowdExportFull.get();
    }
}

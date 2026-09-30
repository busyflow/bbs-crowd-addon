package mchorse.bbs_mod.ui.film.crowds;

import mchorse.bbs_mod.film.crowds.Crowd;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.framework.elements.UIScrollView;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIOverlayPanel;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.UIConstants;

/**
 * Modal overlay panel for full crowd configuration, accessible directly from
 * quick action buttons, hotkeys, or menus.
 */
public class UICrowdOverlayPanel extends UIOverlayPanel
{
    private final Crowd crowd;
    private final Runnable onEdit;
    private final UIScrollView body;
    private final UICrowdSettings settings;

    public UICrowdOverlayPanel(Crowd crowd, Runnable onEdit)
    {
        super(IKey.constant("Crowd Configuration"));

        this.crowd = crowd;
        this.onEdit = onEdit;

        this.body = UI.scrollView(UIConstants.MARGIN, UIConstants.SCROLL_PADDING);
        this.body.full(this.content);
        this.settings = new UICrowdSettings(onEdit);
        this.settings.setCrowd(crowd);

        this.body.add(this.settings);
        this.content.add(this.body);
    }
}

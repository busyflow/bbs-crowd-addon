package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.CrowdForm;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.forms.editors.panels.UICrowdFormPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

/**
 * Dedicated UIForm editor for CrowdForm.
 */
public class UICrowdForm extends UIForm<CrowdForm>
{
    public final UICrowdFormPanel crowdPanel;

    public UICrowdForm()
    {
        super();

        this.crowdPanel = new UICrowdFormPanel(this);
        this.defaultPanel = this.crowdPanel;

        this.registerPanel(this.defaultPanel, IKey.constant("Crowd"), Icons.USER);
        this.registerDefaultPanels();
    }
}

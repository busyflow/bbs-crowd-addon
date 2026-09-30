package mchorse.bbs_mod.ui.framework.elements.input.keyframes.factories;

import mchorse.bbs_mod.actions.crowd.CrowdTexture;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITexturePicker;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UIKeyframes;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIFolderOverlayPanel;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIOverlay;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.UIConstants;
import mchorse.bbs_mod.utils.colors.Colors;
import mchorse.bbs_mod.utils.keyframes.Keyframe;

/**
 * Apple/1UI-grade discrete crowd texture editor with explicit texture and random folder modes.
 */
public class UICrowdTextureKeyframeFactory extends UIKeyframeFactory<CrowdTexture>
{
    private final UIToggle random;
    private final UIToggle recursive;
    private final UIButton texture;
    private final UIButton clearTexture;
    private final UIElement textureRow;
    private final UIButton folder;
    private final UIButton clearFolder;
    private final UIElement folderRow;
    private final UIElement content;

    public UICrowdTextureKeyframeFactory(Keyframe<CrowdTexture> keyframe, UIKeyframes editor)
    {
        super(keyframe, editor);

        if (keyframe.getValue() == null)
        {
            keyframe.setValue(new CrowdTexture());
        }

        keyframe.setDuration(0F);
        this.duration.setVisible(false);

        this.random = new UIToggle(IKey.constant("Randomize from Folder"), b ->
        {
            this.edit(v -> v.random = b.getValue());
            this.refresh();
        });
        this.random.tooltip(IKey.constant("When checked, randomly distributes textures from a folder across members.\nWhen unchecked, applies a single specific texture to the crowd."));

        this.texture = new UIButton(IKey.EMPTY, b -> this.pickTexture());
        this.texture.tooltip(IKey.constant("Select a specific texture/skin for the crowd."));
        this.clearTexture = new UIButton(IKey.constant("X"), b ->
        {
            this.edit(v -> v.texture = null);
            this.refresh();
        });
        this.clearTexture.tooltip(IKey.constant("Clear texture selection"));
        this.clearTexture.color(Colors.NEGATIVE);
        this.textureRow = UI.row(4, this.texture, this.clearTexture.w(24));

        this.folder = new UIButton(IKey.EMPTY, b -> this.pickFolder());
        this.folder.tooltip(IKey.constant("Pick a folder of textures to assign deterministically across members."));
        this.clearFolder = new UIButton(IKey.constant("X"), b ->
        {
            this.edit(v -> v.folder = null);
            this.refresh();
        });
        this.clearFolder.tooltip(IKey.constant("Clear folder selection"));
        this.clearFolder.color(Colors.NEGATIVE);
        this.folderRow = UI.row(4, this.folder, this.clearFolder.w(24));

        this.recursive = new UIToggle(IKey.constant("Include Subfolders"), b -> this.edit(v -> v.recursive = b.getValue()));
        this.recursive.tooltip(IKey.constant("Scan and include textures contained in nested subdirectories."));

        this.content = UI.column(
            UIConstants.MARGIN,
            UI.label(IKey.constant("Crowd Texture")),
            UI.label(IKey.constant("Texture modifications take effect immediately at this keyframe.")),
            this.random.marginTop(UIConstants.SECTION_GAP),
            this.textureRow,
            this.folderRow,
            this.recursive
        );

        this.scroll.add(this.content);
        this.refresh();
    }

    private void pickTexture()
    {
        UITexturePicker.open(this.getContext(), this.keyframe.getValue().texture, link ->
        {
            this.edit(v ->
            {
                v.texture = link;
                v.random = false;
            });
            this.refresh();
        });
    }

    private void pickFolder()
    {
        CrowdTexture value = this.keyframe.getValue();
        UIFolderOverlayPanel panel = new UIFolderOverlayPanel(
            IKey.constant("Crowd texture keyframe folder"),
            IKey.constant("Pick a folder containing PNG textures. Member choices stay deterministic."),
            folder ->
            {
                if (folder != null)
                {
                    this.edit(v ->
                    {
                        v.folder = folder;
                        v.random = true;
                    });
                    this.refresh();
                }
            }
        );

        if (value.folder != null)
        {
            panel.list.setPath(value.folder);
        }

        UIOverlay.addOverlay(this.getContext(), panel, 320, 0.8F);
    }

    private void edit(java.util.function.Consumer<CrowdTexture> consumer)
    {
        this.keyframe.preNotify();
        consumer.accept(this.keyframe.getValue());
        this.keyframe.postNotify();
    }

    private void refresh()
    {
        CrowdTexture value = this.keyframe.getValue();
        Link selectedTexture = value.texture;
        Link selectedFolder = value.folder;

        this.random.setValue(value.random);
        this.recursive.setValue(value.recursive);

        this.texture.label = IKey.constant(selectedTexture == null ? "Select texture..." : selectedTexture.toString());
        this.folder.label = IKey.constant(selectedFolder == null ? "Select texture folder..." : selectedFolder.toString());

        this.textureRow.setVisible(!value.random);
        this.folderRow.setVisible(value.random);
        this.recursive.setVisible(value.random);

        this.content.resize();
        this.scroll.resize();
    }

    @Override
    public void update()
    {
        super.update();
        this.refresh();
    }
}

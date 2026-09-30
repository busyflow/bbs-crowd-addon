package mchorse.bbs_mod.ui.film.crowds;

import mchorse.bbs_mod.actions.types.crowd.CrowdFormation;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.crowds.Crowd;
import mchorse.bbs_mod.film.replays.Replay;
import mchorse.bbs_mod.forms.FormUtils;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.clips.area.AreaBrush;
import mchorse.bbs_mod.ui.forms.UIFormPalette;
import mchorse.bbs_mod.ui.forms.UINestedEdit;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.UISection;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextbox;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIFolderOverlayPanel;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIListOverlayPanel;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIOverlay;
import mchorse.bbs_mod.ui.framework.elements.utils.UILabel;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.UIConstants;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import mchorse.bbs_mod.utils.colors.Colors;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Apple/1UI-grade crowd settings panel.
 *
 * <p>Presents a clean, structured hierarchy for configuring crowd population, actor model/texture
 * options, formation geometries, physics/simulation properties, and contextual ground painting.</p>
 */
public class UICrowdSettings extends UIElement
{
    private static final List<String> MOB_IDS = new ArrayList<>();

    static
    {
        for (RegistryKey<EntityType<?>> key : Registries.ENTITY_TYPE.getKeys())
        {
            MOB_IDS.add(key.getValue().toString());
        }

        MOB_IDS.sort(Comparator.comparing((a) -> a));
    }

    private Crowd crowd;
    private final Runnable onEdit;

    /* Overview & Population */
    private final UITextbox name;
    private final UIToggle enabled;
    private final UITrackpad count;
    private final UITrackpad seed;
    private final UITrackpad start;

    /* Actor & Appearance */
    private final UIToggle useActorForm;
    private final UIButton mobType;
    private final UIElement mobRow;
    private final UINestedEdit actorForm;
    private final UIElement modelRow;
    private final UIToggle randomTextures;
    private final UIButton textureFolder;
    private final UIButton clearTextureFolder;
    private final UIElement textureFolderRow;
    private final UIButton armor;

    /* Formation & Placement */
    private final UIButton formation;
    private final UIButton anchor;
    private final UITrackpad spacing;
    private final UITrackpad holeRadius;
    private final UIElement holeRow;

    /* Spawning & Simulation */
    private final UIToggle spawnOnBlock;
    private final UIToggle skipUnsafe;
    private final UIToggle disableAi;
    private final UIToggle randomYaw;

    /* Sections */
    private final UIElement overviewSection;
    private final UIElement actorSection;
    private final UIElement whereSection;
    private final UIElement spawnSection;
    private final UIElement paintSection;

    /* Paint Brush Controls */
    private final UITrackpad brushSize;
    private final UIButton paint;
    private final UIButton erase;
    private final UIButton removeSelection;
    private final UIToggle showOutline;
    private final UILabel paintInfo;

    public UICrowdSettings(Runnable onEdit)
    {
        this.onEdit = onEdit;

        /* 1. Overview & Population */
        this.name = new UITextbox(64, (text) -> this.edit((crowd) -> crowd.name.set(text)));
        this.name.textbox.setPlaceholder(IKey.constant("Crowd Name"));
        this.enabled = new UIToggle(IKey.constant("Enabled"), (b) -> this.edit((crowd) -> crowd.enabled.set(b.getValue())));
        this.count = new UITrackpad((value) -> this.edit((crowd) -> crowd.count.set(value.intValue())));
        this.count.limit(1, 100000).integer().tooltip(IKey.constant("Total number of crowd members in the formation."));
        this.seed = new UITrackpad((value) -> this.edit((crowd) -> crowd.seed.set(value.intValue())));
        this.seed.integer().tooltip(IKey.constant("Random seed ensuring member variations and positions are 100% deterministic."));
        this.start = new UITrackpad((value) -> this.edit((crowd) -> crowd.start.set(value.intValue())));
        this.start.integer().tooltip(IKey.constant("The film tick on which this crowd appears."));

        /* 2. Actor & Appearance */
        this.useActorForm = new UIToggle(IKey.constant("Use BBS 3D Model"), (b) ->
        {
            this.edit((crowd) -> crowd.useActorForm.set(b.getValue()));
            this.updateAppearanceVisibility();
        });
        this.useActorForm.tooltip(IKey.constant("When checked, crowd members use a custom BBS 3D model form.\nWhen unchecked, members spawn as vanilla Minecraft mobs."));

        this.mobType = new UIButton(IKey.EMPTY, (b) -> this.openMobPicker());
        this.mobType.tooltip(IKey.constant("Select the vanilla entity type for crowd members."));
        this.mobRow = this.row("Mob Type", this.mobType);

        this.actorForm = new UINestedEdit((edit) -> this.openFormPicker(edit)).keybinds();
        this.modelRow = this.row("Model", this.actorForm);

        this.randomTextures = new UIToggle(IKey.constant("Randomize Textures"), (b) ->
        {
            this.edit((crowd) -> crowd.randomTextures.set(b.getValue()));
            this.updateAppearanceVisibility();
        });
        this.randomTextures.tooltip(IKey.constant("Randomly pick textures/skins from a folder for each crowd member."));

        this.textureFolder = new UIButton(IKey.EMPTY, (b) -> this.openTextureFolderPicker());
        this.textureFolder.tooltip(IKey.constant("Folder containing skin/texture files for deterministic random assignment."));
        this.clearTextureFolder = new UIButton(IKey.constant("X"), (b) -> this.clearTextureFolder());
        this.clearTextureFolder.tooltip(IKey.constant("Clear texture folder"));
        this.clearTextureFolder.color(Colors.NEGATIVE);
        this.textureFolderRow = UI.row(4, this.textureFolder, this.clearTextureFolder.w(24));

        this.armor = new UIButton(IKey.constant("Armor Loadout..."), (b) -> this.openArmor());
        this.armor.tooltip(IKey.constant("Dress the crowd: weighted armor profiles each member rolls into, mixed per slot."));

        /* 3. Formation & Placement */
        this.formation = new UIButton(IKey.EMPTY, (b) -> this.openFormationMenu());
        this.formation.tooltip(IKey.constant("The geometric pattern or ground shape the crowd occupies."));
        this.anchor = new UIButton(IKey.EMPTY, (b) -> this.openAnchorMenu());
        this.anchor.tooltip(IKey.constant("Replay that serves as the center anchor point for the formation."));
        this.spacing = new UITrackpad((value) -> this.edit((crowd) -> crowd.spacing.set(value.floatValue())));
        this.spacing.limit(0.1D, 64D).values(0.1D, 0.05D, 1D).tooltip(IKey.constant("Distance between individual crowd members in blocks."));
        this.holeRadius = new UITrackpad((value) -> this.edit((crowd) -> crowd.holeRadius.set(value.floatValue())));
        this.holeRadius.limit(0D, 128D).values(0.1D, 0.01D, 0.5D).tooltip(IKey.constant("Inner clearing radius (donut hole) in blocks."));
        this.holeRow = this.row("Donut Hole", this.holeRadius);

        /* 4. Spawning & Simulation */
        this.spawnOnBlock = new UIToggle(IKey.constant("Surface Snap"), (b) -> this.edit((crowd) -> crowd.spawnOnBlock.set(b.getValue())));
        this.spawnOnBlock.tooltip(IKey.constant("Surface snap: raycasts down to place each member firmly on solid blocks."));
        this.skipUnsafe = new UIToggle(IKey.constant("Skip Blocked"), (b) -> this.edit((crowd) -> crowd.skipUnsafe.set(b.getValue())));
        this.skipUnsafe.tooltip(IKey.constant("Skip blocked: members colliding with solid walls or obstacles will not spawn."));
        this.disableAi = new UIToggle(IKey.constant("Disable Mob AI"), (b) -> this.edit((crowd) -> crowd.disableAi.set(b.getValue())));
        this.disableAi.tooltip(IKey.constant("Disable Mob AI: disables vanilla mob wander and brain pathfinding to save CPU and keep members in formation."));
        this.randomYaw = new UIToggle(IKey.constant("Random Yaw"), (b) -> this.edit((crowd) -> crowd.randomYaw.set(b.getValue())));
        this.randomYaw.tooltip(IKey.constant("Random initial yaw: introduces slight heading variations so members don't look completely robotic."));

        /* 5. Ground Painting */
        this.brushSize = new UITrackpad((value) -> this.edit((crowd) -> crowd.brushSize.set(value.intValue())));
        this.brushSize.limit(1, 64).integer().tooltip(IKey.constant("Radius of the terrain painting brush in blocks."));
        this.paint = new UIButton(IKey.constant("Paint"), (b) -> this.toggleBrush(false));
        this.erase = new UIButton(IKey.constant("Erase"), (b) -> this.toggleBrush(true));
        this.removeSelection = new UIButton(IKey.constant("Clear Canvas"), (b) ->
        {
            this.edit(Crowd::clearCells);

            if (AreaBrush.onFinishStroke != null)
            {
                AreaBrush.onFinishStroke.run();
            }
        });
        this.removeSelection.color(Colors.NEGATIVE);
        this.showOutline = new UIToggle(IKey.constant("Show Outline"), (b) -> this.edit((crowd) -> crowd.showOutline.set(b.getValue())));
        this.showOutline.tooltip(IKey.constant("Draw the painted ground's boundary outline in the viewport."));
        this.paintInfo = UI.label(IKey.EMPTY);

        /* Assembling Sections */
        this.overviewSection = this.section("Crowd Population",
            this.row("Name", this.name),
            this.enabled,
            this.row("Count", this.count),
            this.row("Seed", this.seed),
            this.row("Start Tick", this.start)
        );

        this.actorSection = this.section("Actor & Appearance",
            this.useActorForm,
            this.mobRow,
            this.modelRow,
            this.randomTextures,
            this.textureFolderRow,
            this.armor
        );

        this.whereSection = this.section("Formation & Placement",
            this.row("Formation", this.formation),
            this.row("Anchor", this.anchor),
            this.row("Spacing", this.spacing),
            this.holeRow
        );

        this.spawnSection = this.section("Spawning & Simulation",
            UI.row(4, this.spawnOnBlock, this.skipUnsafe),
            UI.row(4, this.disableAi, this.randomYaw)
        );

        this.paintSection = this.section("Painted Ground Canvas",
            this.row("Brush Size", this.brushSize),
            UI.row(4, this.paint, this.erase),
            UI.row(4, this.showOutline, this.removeSelection),
            this.paintInfo
        );

        this.column(UIConstants.MARGIN).vertical().stretch();

        this.add(
            this.overviewSection,
            this.actorSection,
            this.whereSection,
            this.spawnSection
        );
    }

    private void openArmor()
    {
        if (this.crowd == null)
        {
            return;
        }

        UICrowdArmorOverlayPanel panel = new UICrowdArmorOverlayPanel(this.crowd.armor.get(), () -> this.edit((crowd) -> {}));
        UIOverlay.addOverlay(this.getContext(), panel, 420, 0.85F);
    }

    public Crowd getCrowd()
    {
        return this.crowd;
    }

    public void setCrowd(Crowd crowd)
    {
        this.crowd = crowd;

        AreaBrush.disarm();
        this.setVisible(crowd != null);

        if (crowd != null)
        {
            this.fillData();
        }
    }

    private void edit(Consumer<Crowd> consumer)
    {
        if (this.crowd != null)
        {
            consumer.accept(this.crowd);

            if (this.onEdit != null)
            {
                this.onEdit.run();
            }
        }
    }

    private void fillData()
    {
        Crowd crowd = this.crowd;

        this.name.setText(crowd.name.get());
        this.enabled.setValue(crowd.enabled.get());
        this.start.setValue(crowd.start.get());
        this.refreshAnchorLabel();

        this.mobType.label = IKey.constant(crowd.mobType.get());
        this.useActorForm.setValue(crowd.useActorForm.get());
        this.actorForm.setForm(crowd.actorForm.get());
        this.randomTextures.setValue(crowd.randomTextures.get());
        this.refreshTextureFolderLabel();

        this.count.setValue(crowd.count.get());
        this.seed.setValue(crowd.seed.get());
        this.spacing.setValue(crowd.spacing.get());
        this.refreshFormationLabel();
        this.holeRadius.setValue(crowd.holeRadius.get());
        this.disableAi.setValue(crowd.disableAi.get());
        this.randomYaw.setValue(crowd.randomYaw.get());
        this.spawnOnBlock.setValue(crowd.spawnOnBlock.get());
        this.skipUnsafe.setValue(crowd.skipUnsafe.get());
        this.brushSize.setValue(crowd.brushSize.get());
        this.showOutline.setValue(crowd.showOutline.get());

        this.updateAppearanceVisibility();
    }

    private void updateAppearanceVisibility()
    {
        if (this.crowd == null)
        {
            return;
        }

        boolean useModel = this.useActorForm.getValue();
        this.mobRow.setVisible(!useModel);
        this.modelRow.setVisible(useModel);
        this.randomTextures.setVisible(useModel);
        this.textureFolderRow.setVisible(useModel && this.randomTextures.getValue());

        boolean isCircle = this.crowd.getFormation() == CrowdFormation.CIRCLE;
        this.holeRow.setVisible(isCircle);

        this.resize();
    }

    @Override
    public void render(UIContext context)
    {
        if (this.crowd != null)
        {
            boolean painting = this.crowd.getFormation() == CrowdFormation.PAINT;

            if (!painting)
            {
                AreaBrush.disarm();
            }

            this.setPaintVisible(painting);

            boolean armed = AreaBrush.getCrowd() == this.crowd;

            this.paint.custom = armed && !AreaBrush.isErasing();
            this.paint.customColor = Colors.A100 | Colors.ACTIVE;
            this.erase.custom = armed && AreaBrush.isErasing();
            this.erase.customColor = Colors.A100 | Colors.ACTIVE;
            this.paintInfo.label = IKey.constant(this.crowd.getCells().size() + " blocks painted");
        }

        super.render(context);
    }

    private void setPaintVisible(boolean visible)
    {
        boolean present = this.paintSection.getParent() != null;

        if (visible == present)
        {
            return;
        }

        if (visible)
        {
            this.addAfter(this.whereSection, this.paintSection);
        }
        else
        {
            this.paintSection.removeFromParent();
        }

        this.resize();
    }

    private void toggleBrush(boolean erasing)
    {
        if (AreaBrush.getCrowd() == this.crowd && AreaBrush.isErasing() == erasing)
        {
            AreaBrush.disarm();
            return;
        }

        AreaBrush.arm(this.crowd, erasing);
    }

    /* Pickers */

    private void openFormPicker(boolean editing)
    {
        if (this.crowd == null)
        {
            return;
        }

        UIFormPalette.open(this, editing, this.crowd.actorForm.get(), (form) ->
        {
            this.edit((crowd) -> crowd.actorForm.set(FormUtils.copy(form)));
            this.actorForm.setForm(form);
        });
    }

    private void openTextureFolderPicker()
    {
        if (this.crowd == null)
        {
            return;
        }

        Link current = this.crowd.randomTextureFolder.get();
        UIFolderOverlayPanel panel = new UIFolderOverlayPanel(
            IKey.constant("Random crowd textures folder"),
            IKey.constant("Select a folder containing textures/skins for crowd members."),
            (folder) ->
            {
                if (folder != null)
                {
                    this.edit((crowd) ->
                    {
                        crowd.randomTextureFolder.set(folder);
                        crowd.randomTextures.set(true);
                    });
                    this.fillData();
                }
            }
        );

        if (current != null)
        {
            panel.list.setPath(current);
        }

        UIOverlay.addOverlay(this.getContext(), panel, 320, 0.8F);
    }

    private void clearTextureFolder()
    {
        if (this.crowd != null)
        {
            this.edit((crowd) -> crowd.randomTextureFolder.set(null));
            this.fillData();
        }
    }

    private void refreshTextureFolderLabel()
    {
        Link folder = this.crowd == null ? null : this.crowd.randomTextureFolder.get();
        this.textureFolder.label = IKey.constant(folder == null ? "Select texture folder..." : folder.toString());
    }

    private void openMobPicker()
    {
        if (this.crowd == null)
        {
            return;
        }

        UIListOverlayPanel panel = new UIListOverlayPanel(IKey.constant("Mob type"), (id) ->
        {
            this.edit((crowd) -> crowd.mobType.set(id));
            this.mobType.label = IKey.constant(id);
        });

        panel.addValues(MOB_IDS).setValue(this.crowd.mobType.get());
        UIOverlay.addOverlay(this.getContext(), panel, 240, 300);
    }

    private void openFormationMenu()
    {
        this.getContext().replaceContextMenu((menu) ->
        {
            for (CrowdFormation formation : CrowdFormation.values())
            {
                menu.action(Icons.SHAPES, IKey.constant(formation.title), () ->
                {
                    this.edit((crowd) -> crowd.formation.set(formation.ordinal()));
                    this.refreshFormationLabel();
                    this.updateAppearanceVisibility();
                });
            }
        });
    }

    private void openAnchorMenu()
    {
        Film film = UIFilmPanel.getEditedFilm();

        if (film == null)
        {
            return;
        }

        this.getContext().replaceContextMenu((menu) ->
        {
            menu.action(Icons.CLOSE, IKey.constant("(none - world origin)"), () ->
            {
                this.edit((crowd) -> crowd.anchor.set(-1));
                this.refreshAnchorLabel();
            });

            List<Replay> replays = film.replays.getList();

            for (int i = 0; i < replays.size(); i++)
            {
                int index = i;

                menu.action(Icons.SCENE, IKey.constant(replays.get(i).getName()), () ->
                {
                    this.edit((crowd) -> crowd.anchor.set(index));
                    this.refreshAnchorLabel();
                });
            }
        });
    }

    private void refreshAnchorLabel()
    {
        Film film = UIFilmPanel.getEditedFilm();
        int index = this.crowd == null ? -1 : this.crowd.anchor.get();
        List<Replay> replays = film == null ? List.of() : film.replays.getList();

        this.anchor.label = IKey.constant(index < 0 || index >= replays.size() ? "(none - world origin)" : replays.get(index).getName());
    }

    private void refreshFormationLabel()
    {
        this.formation.label = IKey.constant(this.crowd == null ? "" : this.crowd.getFormation().title);
    }

    /* Layout Helpers */

    private UIElement row(String label, UIElement element)
    {
        return UI.row(4, UI.label(IKey.constant(label)).w(74), element);
    }

    private UIElement section(String label, UIElement... elements)
    {
        UISection section = new UISection(IKey.constant(label));

        section.fields.add(elements);

        return section;
    }
}

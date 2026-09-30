package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_crowd.access.FilmCrowdAccess;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.crowds.Crowd;
import mchorse.bbs_mod.film.crowds.Crowds;
import mchorse.bbs_mod.forms.forms.CrowdForm;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.crowds.UICrowdSettings;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;

/**
 * Dedicated form editor panel for CrowdForm, hosting full UICrowdSettings.
 */
public class UICrowdFormPanel extends UIFormPanel<CrowdForm>
{
    public final UICrowdSettings settings;

    public UICrowdFormPanel(UIForm editor)
    {
        super(editor);

        this.settings = new UICrowdSettings(() ->
        {
            // Changes to crowd settings trigger re-render
        });

        this.options.add(this.settings);
    }

    @Override
    public void startEdit(CrowdForm form)
    {
        super.startEdit(form);

        Film film = UIFilmPanel.getEditedFilm();
        Crowds crowds = FilmCrowdAccess.getCrowds(film);
        Crowd crowd = (crowds == null || form == null) ? null : crowds.byTag(form.crowd.get());

        if (crowd == null && film != null && crowds != null && form != null)
        {
            Crowd newCrowd = crowds.addCrowd();
            newCrowd.name.set("Crowd " + crowds.getList().size());
            form.crowd.set(newCrowd.crowdTag.get());
            crowd = newCrowd;
        }

        this.settings.setCrowd(crowd);
    }
}

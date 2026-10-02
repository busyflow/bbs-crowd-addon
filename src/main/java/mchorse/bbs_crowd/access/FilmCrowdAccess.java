package mchorse.bbs_crowd.access;

import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.crowds.Crowds;

public interface FilmCrowdAccess {
    Crowds bbs_crowd$getCrowds();

    static Crowds getCrowds(Film film) {
        if (film instanceof FilmCrowdAccess access) {
            return access.bbs_crowd$getCrowds();
        }
        return null;
    }

    static Film getEditedFilm() {
        if (mchorse.bbs_mod.BBSModClient.getDashboardIfCreated() == null) {
            return null;
        }
        mchorse.bbs_mod.ui.film.UIFilmPanel panel = mchorse.bbs_mod.BBSModClient.getDashboard().getPanel(mchorse.bbs_mod.ui.film.UIFilmPanel.class);
        return panel == null ? null : panel.getData();
    }
}

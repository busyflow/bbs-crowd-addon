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
}

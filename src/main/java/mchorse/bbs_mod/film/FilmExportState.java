package mchorse.bbs_mod.film;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Who is presently exporting a film to video.
 *
 * <p>Exporting happens on the client - it is frames off the screen - but some of what the server
 * does has to differ between a take and a shot being built, the crowd's population most of all.
 * The client says when it starts and stops, and this is where that is kept.</p>
 */
public final class FilmExportState
{
    private static final Set<UUID> EXPORTING = ConcurrentHashMap.newKeySet();

    private FilmExportState()
    {
    }

    public static void set(UUID player, boolean exporting)
    {
        if (exporting)
        {
            EXPORTING.add(player);
        }
        else
        {
            EXPORTING.remove(player);
        }
    }

    public static void clear(UUID player)
    {
        EXPORTING.remove(player);
    }

    public static boolean isAnyExporting()
    {
        return !EXPORTING.isEmpty();
    }

    public static boolean isExporting(UUID player)
    {
        return player != null && EXPORTING.contains(player);
    }
}

package result;

import model.PublicGameData;

import java.util.Collection;

public record ListGamesResult(
        Collection<PublicGameData> games
) { }

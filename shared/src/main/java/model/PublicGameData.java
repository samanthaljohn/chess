package model;

public record PublicGameData(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName
) { }

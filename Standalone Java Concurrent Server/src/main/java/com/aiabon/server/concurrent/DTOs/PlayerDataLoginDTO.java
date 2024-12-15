package com.aiabon.server.concurrent.DTOs;

/// This data transfer object is used to store the player's id and name used for creating a new player object
public record PlayerDataLoginDTO(int playerId, String playerName)
{
}

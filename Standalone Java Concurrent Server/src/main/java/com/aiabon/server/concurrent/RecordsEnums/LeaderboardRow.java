package com.aiabon.server.concurrent.RecordsEnums;

// A class to represent a row in the leaderboard
public record LeaderboardRow(String name, int wins, int losses)
{
}

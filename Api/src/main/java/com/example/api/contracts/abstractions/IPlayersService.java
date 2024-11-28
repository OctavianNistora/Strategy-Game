package com.example.api.contracts.abstractions;

import com.example.api.contracts.DTOs.CreatePlayerDTO;

public interface IPlayersService {
    void createPlayer(CreatePlayerDTO player);
}

package com.example.api.business.services;


import com.example.api.data.access.entities.Player;
import com.example.api.data.access.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerService
{
    @Autowired
    private PlayerRepository playerRepository;

    public Optional<Player> getPlayerByUsername(String username)
    {
        return playerRepository.findByUsername(username);
    }
}

package com.example.api.business.services;

import com.example.api.data.access.entities.Game;
import com.example.api.data.access.entities.GamePlayer;
import com.example.api.data.access.entities.Player;
import com.example.api.data.access.repositories.GameRepository;
import com.example.api.data.access.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GamesService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    public List<Game> getAllGames() {
        return (List<Game>) gameRepository.findAll();
    }

    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id);
    }

    public Game createGame(String name, Long winnerId, Set<Long> playerIds) {
        Game game = new Game();
        game.setName(name);

        if (winnerId != null) {
            Player winner = playerRepository.findById(winnerId)
                    .orElseThrow(() -> new RuntimeException("Winner not found"));
            game.setWinner(winner);
        }

        game = gameRepository.save(game);

        Game finalGame = game;
        Set<GamePlayer> players = playerIds.stream().map(playerId -> {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("Player not found"));
            GamePlayer gamePlayer = new GamePlayer();
            gamePlayer.setGame(finalGame);
            gamePlayer.setPlayer(player);
            return gamePlayer;
        }).collect(Collectors.toSet());

        game.setPlayers(players);

        return gameRepository.save(game);
    }
}

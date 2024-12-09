package com.example.api.business.services;

import com.example.api.contracts.DTOs.LeaderboardRowDTO;
import com.example.api.data.access.entities.Game;
import com.example.api.data.access.entities.GamePlayer;
import com.example.api.data.access.entities.Player;
import com.example.api.data.access.repositories.GamePlayerRepository;
import com.example.api.data.access.repositories.GameRepository;
import com.example.api.data.access.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GamesService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    public List<Game> getAllGames() {
        return (List<Game>) gameRepository.findAll();
    }

    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id);
    }

    public Game createGame(String name, Long winnerId, Set<Long> playerIds) {
        Game game = new Game();
        game.setName(name);

        // Set the winner if provided
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

            gamePlayerRepository.save(gamePlayer);

            return gamePlayer;
        }).collect(Collectors.toSet());

        game.setPlayers(players);

        return gameRepository.save(game);
    }
    public List<LeaderboardRowDTO> getLeaderboardForPlayer(Long playerId) {
        Player caller = playerRepository.findById(playerId).orElseThrow();
        List<Player> players = new ArrayList<>();

        playerRepository.findAll().forEach(players::add);
        List<Player> topPlayers = players.stream()
                .filter(p -> !p.getName().equals(caller.getName()))
                .sorted(Comparator.comparingInt(p -> p.getGamesWon().size()))
                .limit(15)
                .toList();
        List<Player> result = new ArrayList<>();
        result.add(caller);
        result.addAll(topPlayers);
        LeaderboardRowDTO[] rows = result.stream().map(p -> new LeaderboardRowDTO(
                p.getName(),
                p.getGamesWon().size(),
                p.getGamesPlayed().size() - p.getGamesWon().size()
        )).toArray(LeaderboardRowDTO[]::new);
        return Arrays.stream(rows).toList();
    }
}

package com.example.api.controllers;

import com.example.api.business.services.GamesService;
import com.example.api.data.access.entities.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Controller for managing game-related API requests.
 * Provides endpoints for retrieving, creating, and managing games.
 */
@RestController
@RequestMapping("/api/games")
public class GamesController {

    @Autowired
    private GamesService gamesService;

    /**
     * Retrieves a list of all games.
     *
     * @return A ResponseEntity containing a list of all {@link Game} entities.
     */
    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gamesService.getAllGames();
        return ResponseEntity.ok(games);
    }

    /**
     * Retrieves a game by its ID.
     *
     * @param id The ID of the game to retrieve.
     * @return A ResponseEntity containing the {@link Game} if found,
     * or a 404 Not Found status if the game does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable Long id) {
        Optional<Game> game = gamesService.getGameById(id);
        return game.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new game.
     *
     * @param name      The name of the game.
     * @param winnerId  (Optional) The ID of the winning player, if any.
     * @param playerIds A set of IDs representing the players in the game.
     * @return A ResponseEntity containing the created {@link Game} entity,
     * or a 400 Bad Request status if the input data is invalid.
     */
    @PostMapping
    public ResponseEntity<String> createGame(@RequestParam String name,
                                           @RequestParam(required = false) Long winnerId,
                                           @RequestParam Set<Long> playerIds) {
        try {
            Game game = gamesService.createGame(name, winnerId, playerIds);
            return ResponseEntity.ok("success");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
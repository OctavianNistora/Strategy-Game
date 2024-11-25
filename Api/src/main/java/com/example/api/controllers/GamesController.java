package com.example.api.controllers;

import com.example.api.business.services.GamesService;
import com.example.api.data.access.entities.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/games")
public class GamesController {

    @Autowired
    private GamesService gamesService;

    @GetMapping
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = gamesService.getAllGames();
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable Long id) {
        Optional<Game> game = gamesService.getGameById(id);
        return game.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Game> createGame(@RequestParam String name,
                                           @RequestParam(required = false) Long winnerId,
                                           @RequestParam Set<Long> playerIds) {
        try {
            Game game = gamesService.createGame(name, winnerId, playerIds);
            return ResponseEntity.ok(game);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}

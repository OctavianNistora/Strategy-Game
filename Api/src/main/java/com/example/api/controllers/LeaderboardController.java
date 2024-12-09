package com.example.api.controllers;

import com.example.api.business.services.GamesService;
import com.example.api.contracts.DTOs.LeaderboardRowDTO;
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
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    @Autowired
    private GamesService gamesService;

    @GetMapping("/{playerId}")
    public ResponseEntity<List<LeaderboardRowDTO>> GetLeaderboardsWithPlayer(@PathVariable Long playerId) {
        try {
            var rows = gamesService.getLeaderboardForPlayer(playerId);
            if (rows.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            return ResponseEntity.ok(rows);
        }
        catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
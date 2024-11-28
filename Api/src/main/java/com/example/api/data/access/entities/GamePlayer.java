package com.example.api.data.access.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "GamePlayer")
public class GamePlayer implements Serializable {
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Id
    @ManyToOne
    @JoinColumn(name = "gameId")
    private Game game;

    @Id
    @ManyToOne
    @JoinColumn(name = "playerId")
    private Player player;

    // Getters and Setters
}
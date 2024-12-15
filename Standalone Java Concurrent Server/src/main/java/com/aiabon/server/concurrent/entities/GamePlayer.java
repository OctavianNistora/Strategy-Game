package com.aiabon.server.concurrent.entities;

import jakarta.persistence.*;

import java.io.Serializable;

@IdClass(GamePlayerId.class)
@Entity
@Table(name = "game_player")
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
    @JoinColumn(name = "game_id")
    private Game game;

    @Id
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    // Getters and Setters
}
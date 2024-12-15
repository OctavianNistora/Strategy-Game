package com.aiabon.server.concurrent.entities;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "player")
public class Player {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Game> getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(Set<Game> gamesWon) {
        this.gamesWon = gamesWon;
    }

    public Set<GamePlayer> getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(Set<GamePlayer> gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String name;
    private String password;

    @OneToMany(mappedBy = "winner")
    private Set<Game> gamesWon;

    @OneToMany(mappedBy = "player")
    private Set<GamePlayer> gamesPlayed;


}
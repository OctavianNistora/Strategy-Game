package com.example.api.data.access.entities;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Player")
public class Player implements UserDetails {
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
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
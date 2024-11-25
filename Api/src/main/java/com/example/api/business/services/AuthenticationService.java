package com.example.api.business.services;

import com.example.api.contracts.DTOs.LoginPlayerDTO;
import com.example.api.contracts.DTOs.RegisterPlayerDTO;
import com.example.api.data.access.entities.Player;
import com.example.api.data.access.repositories.PlayerRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final PlayerRepository playerRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            PlayerRepository playerRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Player signup(RegisterPlayerDTO input) {
        Player player = new Player();
        player.setName(input.name());
        player.setPassword(passwordEncoder.encode(input.password()));
        player.setUsername(input.username());

        return playerRepository.save(player);
    }

    public Player authenticate(LoginPlayerDTO input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.username(),
                        input.password()
                )
        );

        return playerRepository.findByUsername(input.username())
                .orElseThrow();
    }
}

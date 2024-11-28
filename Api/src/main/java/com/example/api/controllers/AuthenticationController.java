package com.example.api.controllers;

import com.example.api.business.services.AuthenticationService;
import com.example.api.business.services.JwtService;
import com.example.api.contracts.DTOs.LoginPlayerDTO;
import com.example.api.contracts.DTOs.LoginResponseDTO;
import com.example.api.contracts.DTOs.RegisterPlayerDTO;
import com.example.api.data.access.entities.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication-related API requests.
 * Provides endpoints for user login and registration.
 */
@RequestMapping("/api/auth")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    /**
     * Constructs an AuthenticationController with the required services.
     *
     * @param jwtService            Service for generating and handling JWT tokens.
     * @param authenticationService Service for authentication and user management.
     */
    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    /**
     * Handles user login requests.
     * Authenticates the user and returns a JWT token if successful.
     *
     * @param loginUserDto The login credentials provided by the user.
     * @return A ResponseEntity containing a {@link LoginResponseDTO} with the JWT token and expiration time.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticate(@RequestBody LoginPlayerDTO loginUserDto) {
        Player authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponseDTO loginResponse = new LoginResponseDTO(jwtToken, jwtService.getExpirationTime());
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Handles user registration requests.
     * Registers a new user in the system.
     *
     * @param registerUserDto The registration details provided by the user.
     * @return A ResponseEntity containing the newly registered {@link Player}.
     */
    @PostMapping("/signup")
    public ResponseEntity<Player> register(@RequestBody RegisterPlayerDTO registerUserDto) {
        Player registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }
}
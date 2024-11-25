package com.example.api.controllers;
import com.example.api.business.services.AuthenticationService;
import com.example.api.business.services.JwtService;
import com.example.api.contracts.DTOs.LoginPlayerDTO;
import com.example.api.contracts.DTOs.LoginResponseDTO;
import com.example.api.contracts.DTOs.RegisterPlayerDTO;
import com.example.api.data.access.entities.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticate(@RequestBody LoginPlayerDTO loginUserDto) {
        Player authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponseDTO loginResponse = new LoginResponseDTO(jwtToken, jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<Player> register(@RequestBody RegisterPlayerDTO registerUserDto) {
        Player registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }
}

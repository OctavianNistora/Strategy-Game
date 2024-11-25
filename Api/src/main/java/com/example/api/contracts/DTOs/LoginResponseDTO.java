package com.example.api.contracts.DTOs;

public record LoginResponseDTO(String token, long expiresIn) {
}

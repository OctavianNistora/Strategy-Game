package com.example.api.contracts.DTOs;

import com.example.api.contracts.DTOs.PlayerCommandResponseDataDTO;

public record PlayerCommandResponseDTO(String command, PlayerCommandResponseDataDTO playerCommandResponse) {
}

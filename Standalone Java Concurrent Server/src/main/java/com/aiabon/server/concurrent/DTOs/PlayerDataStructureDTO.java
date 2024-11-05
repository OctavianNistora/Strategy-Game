package com.aiabon.server.concurrent.DTOs;

/// This data transfer object is used to store the data of the material type and the structure id used for the transfer between the player and the structure (store or steal)
public record PlayerDataStructureDTO(int materialType, int structureId)
{
}

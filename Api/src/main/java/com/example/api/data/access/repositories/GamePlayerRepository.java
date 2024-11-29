package com.example.api.data.access.repositories;

import com.example.api.data.access.entities.GamePlayer;
import org.springframework.data.repository.CrudRepository;

public interface GamePlayerRepository extends CrudRepository<GamePlayer, Long> {
}

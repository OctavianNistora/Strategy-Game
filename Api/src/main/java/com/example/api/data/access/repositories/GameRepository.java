package com.example.api.data.access.repositories;

import com.example.api.data.access.entities.Game;
import org.springframework.data.repository.CrudRepository;

public interface GameRepository extends CrudRepository<Game, Long> {
}

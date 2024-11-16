package main.java.com;

import main.java.com.entity.Game;
import main.java.com.entity.Player;
import main.java.com.repository.Repository;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Repository<Player> playerRepository = new Repository<>(SessionFactorySingleton.getInstance(), Player.class);
        Repository<Game> gameRepository = new Repository<>(SessionFactorySingleton.getInstance(), Game.class);
//        Player player1 = new Player();
//        player1.setUsername("john_doe");
//        player1.setName("John Doe");
//        player1.setPassword("secure_password");
//        Player player2 = new Player();
//        player2.setUsername("jane_doe");
//        player2.setName("Jane Doe");
//        player2.setPassword("secure");
//        Game game = new Game();
//        List<Player> players = new ArrayList<>();
//        players.add(player1);
//        players.add(player2);
//        game.setParticipants(players);
//        game.setWinner(player1);
//        playerRepository.save(player1);
//        playerRepository.save(player2);
//        gameRepository.save(game);

//        Player fetchedPlayer = playerRepository.findAll().stream().filter(x -> Objects.equals(x.getName(), "John Doe")).findFirst().get();
//        System.out.println(fetchedPlayer.getName());
//        System.out.println(fetchedPlayer.getGamesWon());
//        List<Player> allPlayers = playerRepository.findAll();
//        Game game = new Game();
//        game.setParticipants(allPlayers);
//        game.setWinner(allPlayers.get(1));
//        game.setName("Bozogame");
//        gameRepository.save(game);

        List<Game> games = gameRepository.findAll();
        List<Player> players = playerRepository.findAll();
    }
}

package main.java.com;

import main.java.com.entity.Game;
import main.java.com.entity.Player;
import main.java.com.repository.Repository;

public class Main {
    public static void main(String[] args) {
        Repository<Player> playerRepository = new Repository<>(SessionFactorySingleton.getInstance(), Player.class);
        Player player = new Player();
        player.setUsername("john_doe2");
        player.setName("John Doe2");
        player.setPassword("secure_password2");
        playerRepository.save(player);
        Player fetchedPlayer = playerRepository.findById(player.getId());
        System.out.println("Fetched Player: " + fetchedPlayer);
        System.out.println(fetchedPlayer.getUsername());
    }
}

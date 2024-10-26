# Strategy-Game

## Description:
A strategy game in which players have to collect materials in order to build a multi-stage structure.<br/>
Players can steal materials from the current stage of another player's structure, but cannot steal materials from previously completed stages.<br/>
The first player who finishes the final stage wins.<br/>
Players can carry up to 5 materials at a time.<br/>
Materials spawn frequently once the game starts.<br/>

## Tech stack:
Java Spring<br/>
Java Hibernate (Database)<br/>
Unity (Remote Client)<br/>
Java Swing (Local Client)<br/>

## Concurrency problems (based on complexity of solution)
Low<br/>
1. Multiple players create a game<br/>
Solution: The method that is used to create the game is synchronized<br/>
2. Multiple players want to pick up the same material<br/>
Solution: A HashTable is used to prevent multiple players from picking up the same material and acquiring it<br/>

Medium<br/>
1. Some players want to add materials to a structure, and others want to steal materials from it<br/>
Solution: A monitor is used to allow only one player to modify the progress of the structure. If the material can be added, it's added to the structure (and later removed from the player's inventory), and if possible, the stage of the structure advances, resetting the progress of each material.<br/>

High<br/>
1. The game broadcast thread has waited ~30ms and needs to send the state of the current game to all participating players while they are also sending commands<br/>
Solution: The method that broadcasts the state acquires the RequiresStateBroadcastSemaphore semaphore, waits until it can requiresStateBroadcast boolean and then constantly checks if the number of still-active threads has reached 0, then sends the state to all players that are still in the game/match and afterwards releases the RequiresStateBroadcastSemaphore semaphore. All method calls that influence the state of the game have to first check if the requiresStateBroadcast boolean is true. If it's true, the thread waits until the RequiresStateBroadcastSemaphore semaphore can be acquired and immediately releases it. Afterwards it waits to increment the active threads counter, continues it's execution and after it's done it waits to decrement the counter

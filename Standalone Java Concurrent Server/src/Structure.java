import lombok.Getter;
import lombok.Setter;

import java.util.Hashtable;

@Getter
public class Structure
{
    private final int playerId;  // This is the id of the player who is building the structure
    private final double[] position;  // This is the position of the structure (double instead of float for precision)
    @Setter private int stage;   // This is the stage of the structure
    private final Hashtable<MaterialEnum, Integer> progress;    // This is the progress of the structure's current stage

    private final Object progressLock = new Object();  // This is the lock for the structure's progress

    public Structure(int playerId, double[] position)
    {
        this.playerId = playerId;
        this.position = position;
        this.stage = 0;  // The structure starts at stage 0
        this.progress = new Hashtable<>();  // The structure starts with no progress
        for (MaterialEnum material : MaterialEnum.values())
        {
            progress.put(material, 0);  // The structure starts with 0 progress for each material
        }
    }
}

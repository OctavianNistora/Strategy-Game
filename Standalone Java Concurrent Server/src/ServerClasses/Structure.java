package ServerClasses;

import RecordsEnums.MaterialEnum;

import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;

/// Represents a structure that can be built by a player in the game.
///
/// The structure has 3 stages and each stage requires 2 more progress than the previous stage.
/// At the first stage, the structure requires 1 material of each type to taken to the next stage.
///
/// The structure is complete when it reaches the third stage.
public class Structure
{
    // This is the id of the player who is building the structure
    private final int playerId;
    // This is the stage of the structure
    private int stage;
    // This is the progress of the structure's current stage
    private final Hashtable<MaterialEnum, Integer> progress;

    // This is the lock for the structure's progress
    private final Object progressLock = new Object();
    // This is the semaphore for checking if the structure is complete when adding progress
    private final Semaphore addProgressCheckCompleteSemaphore = new Semaphore(1);

    public Structure(int playerId)
    {
        // The structure is built by a player
        this.playerId = playerId;
        // The structure starts at stage 0
        this.stage = 0;
        // The structure starts with no progress
        this.progress = new Hashtable<>();
        // The structure starts with 0 progress for each material
        for (MaterialEnum material : MaterialEnum.values())
        {
            progress.put(material, 0);
        }
    }

    /// Tries to add a material to the structure's current stage and if it succeeds, it locks
    /// the progress in order to check if the structure is complete without any other thread interfering.
    ///
    /// {@code isCompleteAndUnlockProgress()} NEEDS to be called sometime after this method if it succeeds,
    /// in the same thread it was called, to avoid permanently locking this object's method.
    ///
    /// @param material the material type attempted to be added to the structure
    /// @return <code>true</code> if the progress was added, <code>false</code> otherwise
    public boolean tryAddProgressAndThenLockProgress(MaterialEnum material)
    {
        synchronized (progressLock)
        {
            int currentMaterialProgress = progress.get(material);
            if (currentMaterialProgress >= stage * 2 + 1)
            {
                return false;  // The structure cannot have more progress to this material than the current stage allows
            }
            progress.put(material, currentMaterialProgress + 1);
            if (currentMaterialProgress + 1 == stage * 2 + 1)
            {
                tryAdvanceStage();
            }
            addProgressCheckCompleteSemaphore.acquireUninterruptibly();
            return true;
        }
    }

    /// Tries to advance the structure to the next stage if all materials have the required progress
    private void tryAdvanceStage()
    {
        synchronized (progressLock)
        {
            boolean canAdvanceStage = true;
            Collection<Integer> progressValues = progress.values();
            for (Integer materialProgress : progressValues)
            {
                if (materialProgress < stage * 2 + 1)
                {
                    canAdvanceStage = false;
                    break;
                }
            }
            if (!canAdvanceStage)
            {
                // The structure cannot advance to the next stage if any
                // material has less progress than the current stage allows
                return;
            }

            stage++;
            // The structure resets the progress of each material when advancing to the next stage
            progress.forEach((material, _) -> progress.put(material, 0));
        }
    }

    /// Returns whether the structure is complete and unlocks the progress for other threads to continue.
    ///
    /// @return <code>true</code> if the structure is complete, <code>false</code> otherwise
    public boolean isCompleteAndUnlockProgress()
    {
        synchronized (progressLock)
        {
            addProgressCheckCompleteSemaphore.release();
            return stage == 3;  // The structure is complete when it reaches stage 3
        }
    }

    /// Tries to remove a material from the structure's current stage.
    ///
    /// @param material the material type attempted to be removed from the structure
    /// @return <code>true</code> if the material was removed, <code>false</code> otherwise
    public boolean tryRemoveProgress(MaterialEnum material)
    {
        synchronized (progressLock)
        {
            int currentMaterialProgress = progress.get(material);
            if (currentMaterialProgress == 0)
            {
                // The structure cannot have less progress to this material type than 0
                return false;
            }

            progress.put(material, currentMaterialProgress - 1);
            return true;
        }
    }

    public String toString()
    {
        //noinspection StringBufferReplaceableByString
        StringBuilder structureString = new StringBuilder();
        structureString.append("{");
        structureString.append("\"id\":").append(playerId).append(",");
        structureString.append("\"stage\":").append(stage).append(",");
        structureString.append("\"progress\":[");
        structureString.append(progress.get(MaterialEnum.WOOD)).append(",");
        structureString.append(progress.get(MaterialEnum.STONE)).append(",");
        structureString.append(progress.get(MaterialEnum.METAL));
        structureString.append("]");
        structureString.append("}");
        return structureString.toString();
    }
}

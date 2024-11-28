package com.aiabon.server.concurrent.ServerClasses;

import com.aiabon.server.concurrent.RecordsEnums.MaterialEnum;

import java.util.Collection;
import java.util.Hashtable;

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

    /// Tries to add a material to the structure's current stage and if it succeeds,
    /// it advances the stage if all materials have the required progress.
    ///
    /// @param material the material type attempted to be added to the structure
    /// @return <code>true</code> if the progress was added, <code>false</code> otherwise
    public boolean tryAddProgress(MaterialEnum material)
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
            return true;
        }
    }

    /// Tries to advance the structure to the next stage if all materials have the required progress
    private void tryAdvanceStage()
    {
        Collection<Integer> progressValues = progress.values();
        for (Integer materialProgress : progressValues)
        {
            if (materialProgress < stage * 2 + 1)
            {
                return;
            }
        }

        stage++;
        // The structure resets the progress of each material when advancing to the next stage
        progress.forEach((material, m) -> progress.put(material, 0));
    }

    /// Returns whether the structure is complete.
    ///
    /// @return <code>true</code> if the structure is complete, <code>false</code> otherwise
    public boolean isComplete()
    {
        synchronized (progressLock)
        {
            return stage >= 3;  // The structure is complete when it reaches stage 3
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

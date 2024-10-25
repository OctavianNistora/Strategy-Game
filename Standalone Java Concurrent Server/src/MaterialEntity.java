/// Represents a material entity in the game that can be picked up by a player
///
/// @param materialId   ID specific to the game it's instantiated in
/// @param materialType type of material
/// @param position     position of the material in the game
/// @param radius       radius of the material's hit box (furthest distance from the center)
public record MaterialEntity(int materialId, MaterialEnum materialType, double[] position,
                             double radius)
{

    public String toString()
    {
        StringBuilder MaterialEntityString = new StringBuilder();
        MaterialEntityString.append("{");
        MaterialEntityString.append("\"id\":").append(materialId).append(",");
        MaterialEntityString.append("\"type\":\"").append(materialType).append("\",");
        MaterialEntityString.append("\"position\":[").append(position[0]).append(",").append(position[1]).append("]");
        MaterialEntityString.append("}");
        return MaterialEntityString.toString();
    }
}

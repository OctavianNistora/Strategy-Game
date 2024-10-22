import lombok.Getter;

@Getter
public class MaterialEntity
{
    private final int materialId;  // This is the id specific to the game it's instantiated in
    private final MaterialEnum materialType;  // This is the type of material
    private final double[] position;  // This is the position of the material (double instead of float for precision)
    private final int radius;  // This is the radius of the material's hit box (furthest distance from the center)

    public MaterialEntity(int materialId, MaterialEnum materialType, double[] position)
    {
        this.materialId = materialId;
        this.materialType = materialType;
        this.position = position;
        this.radius = 1;  // The radius is set to 1 by default
    }
}

using UnityEngine;
using UnityEngine.Serialization;
using UnityEngine.UI;

public class StructureManager : MonoBehaviour
{
    public Renderer stage1PartRenderer;
    public Renderer stage2PartRenderer;
    public Renderer stage3PartRenderer;
    public Renderer stage4PartRenderer;
    public Image woodBar;
    public Image stoneBar;
    public Image ironBar;
    
    public void SetColor(Color color)
    {
        stage1PartRenderer.material.color = color;
        stage2PartRenderer.material.color = color;
        stage3PartRenderer.material.color = color;
        stage4PartRenderer.material.color = color;
    }
    
    public void SetResourceBar(float wood, float stone, float iron)
    {
        woodBar.fillAmount = wood;
        stoneBar.fillAmount = stone;
        ironBar.fillAmount = iron;
    }
    
    public void SetStage(int stage)
    {
        switch (stage)
        {
            case 0:
                stage1PartRenderer.gameObject.SetActive(true);
                stage2PartRenderer.gameObject.SetActive(false);
                stage3PartRenderer.gameObject.SetActive(false);
                stage4PartRenderer.gameObject.SetActive(false);
                break;
            case 1:
                stage1PartRenderer.gameObject.SetActive(true);
                stage2PartRenderer.gameObject.SetActive(true);
                stage3PartRenderer.gameObject.SetActive(false);
                stage4PartRenderer.gameObject.SetActive(false);
                break;
            case 2:
                stage1PartRenderer.gameObject.SetActive(true);
                stage2PartRenderer.gameObject.SetActive(true);
                stage3PartRenderer.gameObject.SetActive(true);
                stage4PartRenderer.gameObject.SetActive(false);
                break;
            case 3:
                stage1PartRenderer.gameObject.SetActive(true);
                stage2PartRenderer.gameObject.SetActive(true);
                stage3PartRenderer.gameObject.SetActive(true);
                stage4PartRenderer.gameObject.SetActive(true);
                break;
        }
    }
}

using System.Globalization;
using UnityEngine;
using UnityEngine.InputSystem;

public class PlayerController : MonoBehaviour
{
    private Rigidbody _rigidbody;
    public TextAreaScript textAreaScript;
    
    void Start()
    {
        _rigidbody = gameObject.GetComponent<Rigidbody>();
    }

    private void FixedUpdate()
    {
        if (_rigidbody.linearVelocity.sqrMagnitude >= float.Epsilon)
        {
            string[] move = new string[3];
            move[0] = "move";
            move[1] = transform.position.x.ToString(CultureInfo.InvariantCulture);
            move[2] = transform.position.z.ToString(CultureInfo.InvariantCulture);
            textAreaScript.Move(move);
        }
    }
    
    public void OnUp(InputAction.CallbackContext context)
    {
        if (context.started)
        {
            _rigidbody.AddForce(10 * Vector3.forward, ForceMode.VelocityChange);
        }
        else if (context.canceled)
        {
            _rigidbody.AddForce(-10 * Vector3.forward, ForceMode.VelocityChange);
        }
    }

    public void OnLeft(InputAction.CallbackContext context)
    {
        if (context.started)
        {
            _rigidbody.AddForce(10 * Vector3.left, ForceMode.VelocityChange);
        }
        else if (context.canceled)
        {
            _rigidbody.AddForce(-10 * Vector3.left, ForceMode.VelocityChange);
        }
    }
    
    public void OnDown(InputAction.CallbackContext context)
    {
        if (context.started)
        {
            _rigidbody.AddForce(10 * Vector3.back, ForceMode.VelocityChange);
        }
        else if (context.canceled)
        {
            _rigidbody.AddForce(-10 * Vector3.back, ForceMode.VelocityChange);
        }
    }
    
    public void OnRight(InputAction.CallbackContext context)
    {
        if (context.started)
        {
            _rigidbody.AddForce(10 * Vector3.right, ForceMode.VelocityChange);
        }
        else if (context.canceled)
        {
            _rigidbody.AddForce(-10 * Vector3.right, ForceMode.VelocityChange);
        }
    }
    
    public void OnPickUp(InputAction.CallbackContext context)
    {
        if (context.started)
        {
            Collider[] colliders = Physics.OverlapSphere(transform.position, 1f);
            foreach (var interactedCollider in colliders)
            {
                if (interactedCollider.gameObject.name.StartsWith("Wood"))
                {
                    textAreaScript.Pickup(new []{"pickup", interactedCollider.gameObject.name.Remove(0, 4)});
                }
                else if (interactedCollider.gameObject.name.StartsWith("Stone"))
                {
                    textAreaScript.Pickup(new []{"pickup", interactedCollider.gameObject.name.Remove(0, 5)});
                }
                else if (interactedCollider.gameObject.name.StartsWith("Metal"))
                {
                    textAreaScript.Pickup(new []{"pickup", interactedCollider.gameObject.name.Remove(0, 5)});
                }
            }
        }
    }
    
    public void OnStorageInteractWood(InputAction.CallbackContext context)
    {
        if (context.started)
        {
            Collider[] colliders = Physics.OverlapSphere(transform.position, 1f);
            foreach (var interactedCollider in colliders)
            {
                if (interactedCollider.gameObject.name.StartsWith("Structure"))
                {
                    var structureId = interactedCollider.gameObject.name.Remove(0, 9);
                    if (structureId.Equals(gameObject.name.Remove(0, 6)))
                    {
                        textAreaScript.Store(new []{"store", "0", structureId});
                    }
                    else
                    {
                        textAreaScript.Steal(new []{"steal", "0", structureId});
                    }
                }
            }
        }
    }
    
    public void OnStorageInteractStone(InputAction.CallbackContext context)
    {
        if (context.started)
        {
            Collider[] colliders = Physics.OverlapSphere(transform.position, 1f);
            foreach (var interactedCollider in colliders)
            {
                if (interactedCollider.gameObject.name.StartsWith("Structure"))
                {
                    var structureId = interactedCollider.gameObject.name.Remove(0, 9);
                    if (structureId.Equals(gameObject.name.Remove(0, 6)))
                    {
                        textAreaScript.Store(new []{"store", "1", structureId});
                    }
                    else
                    {
                        textAreaScript.Steal(new []{"steal", "1", structureId});
                    }
                }
            }
        }
    }
    
    public void OnStorageInteractMetal(InputAction.CallbackContext context)
    {
        if (context.started)
        {
            Collider[] colliders = Physics.OverlapSphere(transform.position, 1f);
            foreach (var interactedCollider in colliders)
            {
                if (interactedCollider.gameObject.name.StartsWith("Structure"))
                {
                    var structureId = interactedCollider.gameObject.name.Remove(0, 9);
                    if (structureId.Equals(gameObject.name.Remove(0, 6)))
                    {
                        textAreaScript.Store(new []{"store", "2", structureId});
                    }
                    else
                    {
                        textAreaScript.Steal(new []{"steal", "2", structureId});
                    }
                }
            }
        }
    }
    
    public void OnTrashWood(InputAction.CallbackContext context)
    {
        if (context.started)
        {
            textAreaScript.Trash(new []{"trash", "0"});
        }
    }
    
    public void OnTrashStone(InputAction.CallbackContext context)
    {
        if (context.started)
        {
            textAreaScript.Trash(new []{"trash", "1"});
        }
    }
    
    public void OnTrashMetal(InputAction.CallbackContext context)
    {
        if (context.started)
        {
            textAreaScript.Trash(new []{"trash", "2"});
        }
    }
}

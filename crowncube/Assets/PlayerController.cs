using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlayerController : MonoBehaviour
{
    public static PlayerController Instance { get; private set; }

    private void Awake()
    {
        Instance = this;
    }

    [SerializeField]
    private GameObject player;
    new Rigidbody rigidbody;
    [SerializeField]
    private GameObject gravityPivot;

    public LayerMask ground;

    [SerializeField]
    new ParticleSystem particleSystemPrefab;

    bool isRunnable = false;
    CrownEventType eventType = CrownEventType.Register;

    private void Start()
    {
        CrownManager.Instance.OnCrownUpdate += onCrownUpdate;
        rigidbody = player.GetComponent<Rigidbody>();

        StartCoroutine(CrownHandleCoroutine());
    }

    private void OnCollisionEnter(Collision collision)
    {
        ParticleSystem particleSystem = Instantiate(particleSystemPrefab);

        particleSystem.transform.position = transform.position;

        Vector3 relativePos = player.transform.position - gravityPivot.transform.position;
        Quaternion rotation = Quaternion.LookRotation(relativePos);
        particleSystem.transform.rotation = rotation;

        particleSystem.Play();

        Destroy(particleSystem.gameObject, 1f);
    }

    private void Update()
    {
        rigidbody.AddForce((player.transform.position - gravityPivot.transform.position).normalized * -9.8f);
    }

    private IEnumerator CrownHandleCoroutine()
    {
        while (true)
        {
            //Debug.Log("isRunning");

            if (!isRunnable)
                yield return new WaitForEndOfFrame();

            switch (eventType)
            {
                case CrownEventType.TouchPress:
                    if (Physics.Raycast(player.transform.position, Vector3.down, 0.51f))
                    {
                        Debug.Log("TouchPressJump");
                        rigidbody.AddForce(0, 50, 0, ForceMode.Force);
                    }
                    //Debug.Log("TouchPress");
                    break;
                case CrownEventType.TurnClockwise:
                    rigidbody.velocity = new Vector3(3, rigidbody.velocity.y, 0);
                    //Debug.Log("TurnClockwise");
                    break;
                case CrownEventType.TurnCounterClockwise:
                    rigidbody.velocity = new Vector3(0, rigidbody.velocity.y, 3);
                    //Debug.Log("TurnCounterClockwise");
                    break;
                default:
                    break;
            }
            isRunnable = false;
            yield return new WaitForEndOfFrame();
        }
    }

    private void onCrownUpdate(CrownEventType eventType)
    {
        isRunnable = true;
        this.eventType = eventType;
        //Debug.Log("isRunnable = true");
    }
}
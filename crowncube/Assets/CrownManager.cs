using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using WebSocketSharp;
using SimpleJson;
using System;
using System.Runtime.InteropServices;
using System.Text;

public enum CrownEventType
{
    Register,
    TurnClockwise,
    TurnCounterClockwise,
    TouchPress,
    TouchRelease
}

public class CrownManager : MonoBehaviour
{
    public static CrownManager Instance { get; private set; }

    [DllImport("user32.dll")]
    private static extern int ShowWindow(int hwnd, int nCmdShow);

    [DllImport("user32.dll")]
    static extern IntPtr GetForegroundWindow();
    [DllImport("user32.dll")]
    static extern int GetWindowText(IntPtr hWnd, StringBuilder text, int count);

    private string GetActiveWindowTitle()
    {
        const int nChars = 256;
        StringBuilder Buff = new StringBuilder(nChars);
        IntPtr handle = GetForegroundWindow();

        if (GetWindowText(handle, Buff, nChars) > 0)
        {
            return Buff.ToString();
        }
        return null;
    }

    public void Show()
    {
        Debug.Log("Show");
        int windowHandle = System.Diagnostics.Process.GetCurrentProcess().MainWindowHandle.ToInt32();
        Console.WriteLine("Showing {0}: has window handle {1}", System.Diagnostics.Process.GetCurrentProcess().ProcessName, windowHandle);
        ShowWindow(windowHandle, 5);
    }

    private void Update()
    {
        if (Input.GetKeyDown(KeyCode.A))
        {
            Debug.Log(GetActiveWindowTitle());
        }
    }

    private void Awake()
    {
        Instance = this;
        //Application.runInBackground = true;
    }

    WebSocket ws;

    public float degree = 0;
    public float degree1 = 0;

    public delegate void CrownEventHandler(CrownEventType eventType);
    public CrownEventHandler OnCrownUpdate;

    [Serializable]
    public class CraftMessage
    {
        public string message_type;
        public string plugin_guid;
        public string execName;
        public int PID;
        public string manifestPath;
    }

    private void Start()
    {
        ws = new WebSocket("ws://127.0.0.1:10134");
        CraftMessage setup = new CraftMessage
        {
            message_type = "register",
            plugin_guid = Guid.NewGuid().ToString(),
            PID = System.Diagnostics.Process.GetCurrentProcess().Id,
            execName = System.Diagnostics.Process.GetCurrentProcess().ProcessName + ".exe",
            manifestPath = ""
        };

        string jsonMessage = JsonUtility.ToJson(setup);
        Debug.Log(jsonMessage);
        ws.Connect();
        ws.Send(jsonMessage);

        ws.OnError += (sender, e) =>
        {
            Debug.Log(e.Message);
        };

        ws.OnMessage += (sender, e) =>
        {
            //Debug.Log("OnMessage: " + e.Data);
            var N = SimpleJson.SimpleJson.DeserializeObject<Dictionary<string, object>>(e.Data);
            //Debug.Log(N["message_type"].ToString());
            CrownEventType eventType = CrownEventType.Register;

            if (N["message_type"].ToString() == "crown_turn_event")
            {
                if (Convert.ToInt32(N["delta"]) > 0)
                {
                    Debug.Log("Detect clockwise turn event");
                    eventType = CrownEventType.TurnClockwise;
                    degree += Convert.ToInt32(N["delta"]);
                    degree1 += Convert.ToInt32(N["ratchet_delta"]);
                    //Debug.Log(Convert.ToInt32(N["delta"]).ToString() + " " + Convert.ToInt32(N["ratchet_delta"]).ToString());
                }
                else if (Convert.ToInt32(N["delta"]) < 0)
                {
                    Debug.Log("Detect counter clockwise turn event");
                    eventType = CrownEventType.TurnCounterClockwise;
                }
            }
            else if (N["message_type"].ToString() == "crown_touch_event")
            {
                if (Convert.ToInt32(N["touch_state"]) == 0)
                {
                    Debug.Log("Detect touch release event");
                    eventType = CrownEventType.TouchRelease;
                }
                else
                {
                    Debug.Log("Detect touch press event");
                    eventType = CrownEventType.TouchPress;
                }
            }
            if (OnCrownUpdate != null)
                OnCrownUpdate.Invoke(eventType);
        };

        ws.OnClose += (sender, e) =>
        {
            Debug.Log("Closed");
        };

        ws.OnOpen += (sender, e) =>
        {
            Debug.Log("OnOpen: " + e.ToString());
        };
    }

    private void OnApplicationPause(bool pause)
    {
        Debug.Log(pause.ToString() + " " + System.Diagnostics.Process.GetCurrentProcess().ProcessName);
    }

    private void OnApplicationQuit()
    {
        ws.Close();
    }
}
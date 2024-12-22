using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using JetBrains.Annotations;
using Newtonsoft.Json;
using TMPro;
using Unity.VisualScripting;
using UnityEngine;
using UnityEngine.InputSystem;
using UnityEngine.Networking;
using WebSocketSharp;

public class TextAreaScript : MonoBehaviour
{
    public Canvas playerUI;
    private TextMeshProUGUI _woodCount;
    private TextMeshProUGUI _stoneCount;
    private TextMeshProUGUI _metalCount;
    
    private int _loggedInPlayerId;
    private object _lock = new();
    private GameStatusDto _gameStatus = null;
    
    public GameObject playersParentObject;
    public GameObject materialsParentObject;
    public GameObject structuresParentObject;
    private int gameStage = 2;
    
    private Dictionary<int, GameObject> _players = new Dictionary<int, GameObject>(4);
    private Dictionary<int, GameObject> _materials = new Dictionary<int, GameObject>();
    private List<StructureManager> _structures = new List<StructureManager>(4);
    
    public Canvas loginScreen;
    public Canvas signupScreen;
    public Canvas gameplayScreen;
    
    private TMP_InputField _loginUsernameField;
    private TMP_InputField _loginPasswordField;
    
    private TMP_InputField _registerNameField;
    private TMP_InputField _registerUsernameField;
    private TMP_InputField _registerPasswordField;
    
    private TMP_InputField _inputField;
    private TextMeshProUGUI _outputText;
    
    private WebSocket _webSocket;
    private bool _appIsRunning = true;
    
    private Canvas _currentScreen;
    private String _serverResponse;
    
    void Start()
    {
        _woodCount = playerUI.transform.Find("Resources/Wood/Count").GetComponent<TextMeshProUGUI>();
        _stoneCount = playerUI.transform.Find("Resources/Stone/Count").GetComponent<TextMeshProUGUI>();
        _metalCount = playerUI.transform.Find("Resources/Metal/Count").GetComponent<TextMeshProUGUI>();
        
        // Negative index to avoid accidentally removing pairs at the start
        var index = -1;
        foreach (Transform player in playersParentObject.transform)
        {
            _players[index] = player.gameObject;
            index--;
        }

        index = -1;
        foreach (Transform material in materialsParentObject.transform)
        {
            _materials[index] = material.gameObject;
            index--;
        }
        
        // Structures are assigned to players in the order of their IDs, so we use a list instead of a dictionary
        for (int i= 0; i< 4; i++)
        {
            _structures.Add(structuresParentObject.transform.Find("Structure" + i).gameObject.GetComponent<StructureManager>());
        }
        
        _currentScreen = loginScreen;
        SetScreen(_currentScreen);
        
        _loginUsernameField = loginScreen.transform.Find("UsernameField").GetComponent<TMP_InputField>();
        _loginPasswordField = loginScreen.transform.Find("PasswordField").GetComponent<TMP_InputField>();
        
        _registerNameField = signupScreen.transform.Find("NameField").GetComponent<TMP_InputField>();
        _registerUsernameField = signupScreen.transform.Find("UsernameField").GetComponent<TMP_InputField>();
        _registerPasswordField = signupScreen.transform.Find("PasswordField").GetComponent<TMP_InputField>();
        
        _inputField = gameplayScreen.transform.Find("InputField").GetComponent<TMP_InputField>();
        _outputText = gameplayScreen.transform.Find("TextArea/OutputText").GetComponent<TextMeshProUGUI>();
        _serverResponse = "";
        _webSocket = new WebSocket("ws://localhost:8080/websocket");
        //_webSocket.SetCredentials("123", "123", true);
        
        _webSocket.OnOpen += (_, _) =>
        {
            Debug.Log("Connected");
            _currentScreen = gameplayScreen;
            if (!_appIsRunning)
            {
                _webSocket.CloseAsync();
            }
        };
        _webSocket.OnMessage += (_, e) =>
        {
            try
            {
                var data = JsonConvert.DeserializeObject<GameStatusDto>(e.Data, new JsonSerializerSettings
                {
                    MissingMemberHandling = MissingMemberHandling.Error
                });
                lock(_lock)
                {
                    _gameStatus = data;
                }
                _serverResponse = JsonConvert.SerializeObject(data, Formatting.Indented);
            }
            catch (Exception ex)
            {
                Debug.Log(ex);
                _serverResponse = e.Data;
            }
        };
        _webSocket.OnClose += (_, e) =>
        {
            Debug.Log("Closed: " + e.Code + " " + e.Reason);
            SetScreen(loginScreen);
        };
        _webSocket.OnError += (_, e) =>
        {
            Debug.Log("Error: " + e.Message);
            _webSocket.CloseAsync();
            SetScreen(loginScreen);
        };
        //_webSocket.ConnectAsync();
        
        InputSystem.actions["UI/Submit"].performed += Submit;
    }

    private void Update()
    {
        SetScreen(_currentScreen);
        if (gameplayScreen.gameObject.activeInHierarchy)
        {
            _outputText.text = _serverResponse;
        }
        
        GameStatusDto gameStatus;
        lock(_lock)
        {
            gameStatus = _gameStatus;
            _gameStatus = null;
        }
        if (gameStatus != null)
        {
            if (gameStatus.gameState != gameStage)
            {
                if (gameStatus.gameState == 0)
                {
                    _currentScreen = gameplayScreen;
                    Debug.Log("Game not started");
                    gameStage = 0;
                }
                else if (gameStatus.gameState == 1)
                {
                    _currentScreen = null;
                    Debug.Log("Game started");
                    gameStage = 1;
                    InitializePlayers(gameStatus.players);
                    InitializeStructures(gameStatus.structures);
                }
                else if (gameStatus.gameState == 2)
                {
                    _currentScreen = gameplayScreen;
                    Debug.Log("Game ended");
                    gameStage = 2;
                }
            }
            else
            {
                if (gameStatus.gameState != 1)
                {
                    return;
                }
                
                foreach (var player in gameStatus.players)
                {
                    if (player.id == _loggedInPlayerId)
                    {
                        _woodCount.text = player.inventory.Where((itemType) => itemType == 0).Count().ToString();
                        _stoneCount.text = player.inventory.Where((itemType) => itemType == 1).Count().ToString();
                        _metalCount.text = player.inventory.Where((itemType) => itemType == 2).Count().ToString();
                        
                        continue;
                    }

                    _players[player.id].transform.position =
                        new Vector3((float)player.position[0], 2.5f, (float)player.position[1]);
                }
                
                ManageMaterialsLifecycle(gameStatus.materials);

                ManageStructuresStage(gameStatus.structures);
            }
        }
    }

    private void OnDestroy()
    {
        _appIsRunning = false;
        _webSocket.Close();
    }
    
    private void SetScreen(Canvas screen)
    {
        if (screen && screen.gameObject.activeInHierarchy) return;
        
        if (screen != loginScreen)
        {
            loginScreen.gameObject.SetActive(false);
        }

        if (screen != signupScreen)
        {
            signupScreen.gameObject.SetActive(false);
        }

        if (screen != gameplayScreen)
        {
            gameplayScreen.gameObject.SetActive(false);
        }
        
        if (screen)
        {
            screen.gameObject.SetActive(true);
        }
    }

    private void InitializePlayers(List<GameStatusPlayerDto> gameStatusPlayers)
    {
        foreach (var player in _players)
        {
            player.Value.SetActive(false);
        }

        var enemyDictionary = new Dictionary <int, GameObject>(_players);
        enemyDictionary.Remove(_loggedInPlayerId);
        foreach (var player in gameStatusPlayers)
        {
            if (player.id == _loggedInPlayerId)
            {
                _players[player.id].SetActive(true);
            }
            else
            {
                if (enemyDictionary.ContainsKey(player.id))
                {
                    enemyDictionary[player.id].SetActive(true);
                    enemyDictionary.Remove(player.id);
                    continue;
                }

                var randomEnemy = enemyDictionary.First();
                
                enemyDictionary.Remove(randomEnemy.Key);
                _players.Remove(randomEnemy.Key);
                _players[player.id] = randomEnemy.Value;
                randomEnemy.Value.name = "Enemy" + player.id;
                randomEnemy.Value.SetActive(true);
            }
        }
    }
    
    public void InitializeStructures(List<GameStatusStructureDto> gameStatusStructureDto)
    {
        var orderedStructures = gameStatusStructureDto.OrderBy(structure => structure.id).ToList();
        for (int i = 0; i < orderedStructures.Count; i++)
        {
            if (orderedStructures[i].id == _loggedInPlayerId)
            {
                _structures[i].SetColor(Color.blue);
            }
            else
            {
                _structures[i].SetColor(Color.red);
            }
            _structures[i].gameObject.name = "Structure" + orderedStructures[i].id;
            _structures[i].SetStage(orderedStructures[i].stage);
            _structures[i].gameObject.SetActive(true);
        }
        for (int i = orderedStructures.Count; i < _structures.Count; i++)
        {
            _structures[i].gameObject.SetActive(false);
        }
    }
    
    private void ManageMaterialsLifecycle(List<GameStatusMaterialDto> gameStatusMaterials)
    {
        var gameStatusMaterialsCopy = new List<GameStatusMaterialDto>(gameStatusMaterials);
        
        Dictionary<int, GameObject> activeMaterials = new Dictionary<int, GameObject>();
        Dictionary<int, GameObject> inactiveMaterials = new Dictionary<int, GameObject>();
        foreach (KeyValuePair<int, GameObject> material in _materials)
        {
            if (material.Value.activeSelf)
            {
                activeMaterials[material.Key] = material.Value;
            }
            else
            {
                inactiveMaterials[material.Key] = material.Value;
            }
        }

        if (activeMaterials.Count > 0)
        {
            foreach (var material in gameStatusMaterialsCopy.ToList())
            {
                if (activeMaterials.ContainsKey(material.id))
                {
                    activeMaterials.Remove(material.id);
                    gameStatusMaterialsCopy.Remove(material);
                    
                    if (activeMaterials.Count == 0)
                    {
                        break;
                    }
                }
            }
            
            if (activeMaterials.Count > 0)
            {
                foreach (var material in activeMaterials)
                {
                    material.Value.SetActive(false);
                    
                    inactiveMaterials[material.Key] = material.Value;
                }
            }
        }
        
        if (gameStatusMaterialsCopy.Count > 0)
        {
            foreach (var material in gameStatusMaterialsCopy)
            {
                var randomInactiveMaterial = inactiveMaterials.First();
                // Material positions are in the range -10 to 10, so we multiply by 5 to get the actual position for our game
                randomInactiveMaterial.Value.transform.position = new Vector3((float)material.position[0] * 5, 2.5f, (float)material.position[1] * 5);
                switch (material.type)
                {
                    case 0:
                        randomInactiveMaterial.Value.name = "Wood" + material.id;
                        randomInactiveMaterial.Value.GetComponent<Renderer>().material.color = new Color(0.6f, 0.3f, 0.1f);
                        break;
                    case 1:
                        randomInactiveMaterial.Value.name = "Stone" + material.id;
                        randomInactiveMaterial.Value.GetComponent<Renderer>().material.color = new Color(0.5f, 0.5f, 0.5f);
                        break;
                    case 2:
                        randomInactiveMaterial.Value.name = "Metal" + material.id;
                        randomInactiveMaterial.Value.GetComponent<Renderer>().material.color = new Color(0.8f, 0.8f, 0.8f);
                        break;
                    default:
                        randomInactiveMaterial.Value.name = "Material" + material.id;
                        randomInactiveMaterial.Value.GetComponent<Renderer>().material.color = new Color(0f, 0f, 0f);
                        break;
                }
                randomInactiveMaterial.Value.SetActive(true);
                
                inactiveMaterials.Remove(randomInactiveMaterial.Key);
                _materials.Remove(randomInactiveMaterial.Key);
                _materials[material.id] = randomInactiveMaterial.Value;
            }
        }
    }
    
    public void ManageStructuresStage(List<GameStatusStructureDto> gameStatusStructures)
    {
        var orderedStructures = gameStatusStructures.OrderBy(structure => structure.id).ToList();
        for (int i = 0; i < orderedStructures.Count; i++)
        {
            _structures[i].SetStage(orderedStructures[i].stage);
            
            _structures[i].SetResourceBar((float)orderedStructures[i].progress[0] / (1 + 2 * orderedStructures[i].stage),
                (float)orderedStructures[i].progress[1] / (1 + 2 * orderedStructures[i].stage),
                (float)orderedStructures[i].progress[2] / (1 + 2 * orderedStructures[i].stage));
        }
    }

    public void LoginWrapper()
    {
        StartCoroutine(Login());
    }

    private IEnumerator Login()
    {
        using var www = UnityWebRequest.Post("http://localhost:8080/api/auth/login", 
            $@"{{""username"":""{_loginUsernameField.text}"",""password"":""{_loginPasswordField.text}""}}", "application/json");

        yield return www.SendWebRequest();
        
        if (www.result == UnityWebRequest.Result.Success)
        {
            _loggedInPlayerId = int.Parse(www.downloadHandler.text);

            foreach (KeyValuePair<int, GameObject> player in _players.ToList())
            {
                if (player.Value.name.StartsWith("Player"))
                {
                    player.Value.name = "Player" + _loggedInPlayerId;
                    
                    GameObject playerToReplace;
                    try
                    {
                        playerToReplace = _players[_loggedInPlayerId];
                    }
                    catch (KeyNotFoundException _)
                    {
                        playerToReplace = null;
                    }

                    _players[_loggedInPlayerId] = player.Value;

                    if (playerToReplace)
                    {
                        _players[player.Key] = playerToReplace;
                    }
                    else
                    {
                        _players.Remove(player.Key);
                    }
                    
                    break;
                }
            }
            
            _webSocket.SetCredentials(_loginUsernameField.text, _loginPasswordField.text, true);
            _webSocket.Connect();
        }
        else
        {
            _loginUsernameField.text = "";
            _loginPasswordField.text = "";
        }
    }

    public void SignUp()
    {
        _currentScreen = signupScreen;
    }
    
    public void RegisterWrapper()
    {
        StartCoroutine(Register());
    }

    private IEnumerator Register()
    {
        using var www = UnityWebRequest.Post("http://localhost:8080/api/auth/signup", 
            $@"{{""name"":""{_registerNameField.text}"",""username"":""{_registerUsernameField.text}"",
                        ""password"":""{_registerPasswordField.text}""}}", "application/json");

        yield return www.SendWebRequest();
        
        if (www.result == UnityWebRequest.Result.Success)
        {
            _currentScreen = loginScreen;
        }
        _registerNameField.text = "";
        _registerUsernameField.text = "";
        _registerPasswordField.text = "";
    }
    
    public void Back()
    {
        _currentScreen = loginScreen;
    }

    void Submit(InputAction.CallbackContext context)
    {
        if (_webSocket.ReadyState == WebSocketState.Open)
        {
            String command = _inputField.text;
            String[] commandParts = command.Split(" ");

            switch (commandParts[0])
            {
                case "help":
                    Help();
                    break;
                case "login":
                    Login(commandParts);
                    break;
                case "listleaderboard":
                    ListLeaderboard(commandParts);
                    break;
                case "listgames":
                    ListGames(commandParts);
                    break;
                case "startgame":
                    StartGame(commandParts);
                    break;
                case "joingame":
                    JoinGame(commandParts);
                    break;
                case "leavegame":
                    LeaveGame(commandParts);
                    break;
                case "ready":
                    Ready(commandParts);
                    break;
                case "unready":
                    Unready(commandParts);
                    break;
                case "move":
                    Move(commandParts);
                    break;
                case "pickup":
                    Pickup(commandParts);
                    break;
                case "trash":
                    Trash(commandParts);
                    break;
                case "store":
                    Store(commandParts);
                    break;
                case "steal":
                    Steal(commandParts);
                    break;
                default:
                    _outputText.text += "Invalid command\n";
                    break;
            }
            _inputField.text = "";
        }
    }
    
    
    private void Help()
    {
        _outputText.text = "Commands:\n" +
                          "login [player ID] [player name]\n" +
                          "listleaderboard\n" +
                          "listgames\n" +
                          "startgame [game name]\n" +
                          "joingame [game ID]\n" +
                          "leavegame\n" +
                          "ready\n" +
                          "unready\n" +
                          "move [x] [y]\n" +
                          "pickup [material ID]\n" +
                          "trash [material type]\n" +
                          "store [material type] [structure ID]\n" +
                          "steal [material type] [structure ID]\n";
    }

    private void Login(string[] commandData)
    {
        if (commandData.Length != 3)
        {
            _outputText.text += "Correct usage: login [player ID] [player name]\n";
            return;
        }

        int playerID;
        try
        {
            playerID = int.Parse(commandData[1]);

        } catch (Exception)
        {
            _outputText.text += "Player ID must be an integer\n";
            return;
        }

        PlayerDataLoginDto playerDataLoginDto = new PlayerDataLoginDto(playerID, commandData[2]);
        String dataJson = JsonConvert.SerializeObject(playerDataLoginDto);
        PlayerCommandDto playerCommandDto = new PlayerCommandDto("login", dataJson);
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);
        
        Debug.Log("Sending: " + commandJson);
        _webSocket.Send(commandJson);
    }

    private void ListLeaderboard(String[] commandData)
    {
        if (commandData.Length != 1)
        {
            _outputText.text += "Correct usage: listleaderboard\n";
            return;
        }

        PlayerCommandDto playerCommandDto = new PlayerCommandDto("listleaderboard", "");
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }

    private void ListGames(String[] commandData)
    {
        if (commandData.Length != 1)
        {
            _outputText.text += "Correct usage: listgames\n";
            return;
        }

        PlayerCommandDto playerCommandDto = new PlayerCommandDto("listgames", "");
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }

    private void StartGame(String[] commandData)
    {
        if (commandData.Length != 2)
        {
            _outputText.text += "Correct usage: startgame [game name]\n";
            return;
        }

        PlayerCommandDto playerCommandDto = new PlayerCommandDto("startgame", commandData[1]);
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }

    private void JoinGame(String[] commandData)
    {
        if (commandData.Length != 2)
        {
            _outputText.text += "Correct usage: joingame [game ID]\n";
            return;
        }

        try
        {
            int.Parse(commandData[1]);
        } catch (Exception)
        {
            _outputText.text += "Game ID must be an integer\n";
            return;
        }

        PlayerCommandDto playerCommandDto = new PlayerCommandDto("joingame", commandData[1]);
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }

    private void LeaveGame(String[] commandData)
    {
        if (commandData.Length != 1)
        {
            _outputText.text += "Correct usage: leavegame\n";
            return;
        }

        PlayerCommandDto playerCommandDto = new PlayerCommandDto("leavegame", "");
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }

    private void Ready(String[] commandData)
    {
        if (commandData.Length != 1)
        {
            _outputText.text += "Correct usage: ready\n";
            return;
        }

        PlayerCommandDto playerCommandDto = new PlayerCommandDto("ready", "");
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }

    private void Unready(String[] commandData)
    {
        if (commandData.Length != 1)
        {
            _outputText.text += "Correct usage: unready\n";
            return;
        }

        PlayerCommandDto playerCommandDto = new PlayerCommandDto("unready", "");
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }

    public void Move(String[] commandData)
    {
        if (commandData.Length != 3)
        {
            _outputText.text += "Correct usage: move [x] [y]\n";
            return;
        }

        double x, y;
        try
        {
            x = double.Parse(commandData[1]);
            y = double.Parse(commandData[2]);
        } catch (Exception)
        {
            _outputText.text += "Coordinates must be doubles\n";
            return;
        }

        PlayerDataMoveDto playerDataMoveDto = new PlayerDataMoveDto(x, y);
        String dataJson = JsonConvert.SerializeObject(playerDataMoveDto);
        PlayerCommandDto playerCommandDto = new PlayerCommandDto("move", dataJson);
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }

    public void Pickup(String[] commandData)
    {
        if (commandData.Length != 2)
        {
            _outputText.text += "Correct usage: pickup [material ID]\n";
            return;
        }

        try
        {
            int.Parse(commandData[1]);
        } catch (Exception)
        {
            _outputText.text += "Material ID must be an integer\n";
            return;
        }

        PlayerCommandDto playerCommandDto = new PlayerCommandDto("pickup", commandData[1]);
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }

    public void Trash(String[] commandData)
    {
        if (commandData.Length != 2)
        {
            _outputText.text += "Correct usage: trash [material type]\n";
            return;
        }

        try
        {
            int.Parse(commandData[1]);
        } catch (Exception)
        {
            _outputText.text += "Material type must be an integer\n";
            return;
        }

        PlayerCommandDto playerCommandDto = new PlayerCommandDto("trash", commandData[1]);
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }

    public void Store(String[] commandData)
    {
        if (commandData.Length != 3)
        {
            _outputText.text += "Correct usage: store [material type] [structure ID]\n";
            return;
        }

        try
        {
            int.Parse(commandData[1]);
        } catch (Exception)
        {
            _outputText.text += "Material type must be an integer\n";
            return;
        }
        try
        {
            int.Parse(commandData[2]);
        } catch (Exception)
        {
            _outputText.text += "Structure ID must be an integer\n";
            return;
        }

        PlayerDataStructureDto playerDataStructureDto = new PlayerDataStructureDto(int.Parse(commandData[1]), int.Parse(commandData[2]));
        String dataJson = JsonConvert.SerializeObject(playerDataStructureDto);
        PlayerCommandDto playerCommandDto = new PlayerCommandDto("store", dataJson);
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }

    public void Steal(String[] commandData)
    {
        if (commandData.Length != 3)
        {
            _outputText.text += "Correct usage: steal [material type] [structure ID]\n";
            return;
        }

        try
        {
            int.Parse(commandData[1]);
        } catch (Exception)
        {
            _outputText.text += "Material type must be an integer\n";
            return;
        }
        try
        {
            int.Parse(commandData[2]);
        } catch (Exception)
        {
            _outputText.text += "Structure ID must be an integer\n";
            return;
        }

        PlayerDataStructureDto playerDataStructureDto = new PlayerDataStructureDto(int.Parse(commandData[1]), int.Parse(commandData[2]));
        String dataJson = JsonConvert.SerializeObject(playerDataStructureDto);
        PlayerCommandDto playerCommandDto = new PlayerCommandDto("steal", dataJson);
        String commandJson = JsonConvert.SerializeObject(playerCommandDto);

        _webSocket.Send(commandJson);
    }
}

public class PlayerDataLoginDto
{
    public int playerId { get; set; }
    public String playerName { get; set; }

    public PlayerDataLoginDto(int playerID, String playerName)
    {
        this.playerId = playerID;
        this.playerName = playerName;
    }
}

public class PlayerDataMoveDto
{
    public double x { get; set; }
    public double y { get; set; }

    public PlayerDataMoveDto(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
}

public class PlayerDataStructureDto
{
    public int materialType { get; set; }
    public int structureId { get; set; }

    public PlayerDataStructureDto(int materialType, int structureID)
    {
        this.materialType = materialType;
        this.structureId = structureID;
    }
}

public class PlayerCommandDto
{
    public String command { get; set; }
    public String data { get; set; }

    public PlayerCommandDto(String command, String data)
    {
        this.command = command;
        this.data = data;
    }
}

public class GameStatusDto
{
    public int id { get; set; }
    public String name { get; set; }
    public List<GameStatusPlayerDto> players { get; set; }
    public List<GameStatusReadyDto> readyStatuses { get; set; }
    public int? winnerId { get; set; }
    public int gameState { get; set; }
    public List<GameStatusMaterialDto> materials { get; set; }
    public List<GameStatusStructureDto> structures { get; set; }
}

public class GameStatusPlayerDto
{
    public int id { get; set; }
    public String name { get; set; }
    public List<double> position { get; set; }
    public List<int> inventory { get; set; }
}

public class GameStatusReadyDto
{
    public int playerId { get; set; }
    public bool ready { get; set; }
}

public class GameStatusMaterialDto
{
    public int id { get; set; }
    public int type { get; set; }
    public List<double> position { get; set; }
}

public class GameStatusStructureDto
{
    public int id { get; set; }
    public int stage { get; set; }
    public List<int> progress { get; set; }
}
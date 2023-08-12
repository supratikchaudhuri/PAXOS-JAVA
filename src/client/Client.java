package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import server.PaxosAPI;
import utils.KeyValuePacket;
import utils.Logger;
import utils.ServerListFetcher;
import utils.Type;

import static utils.Logger.errorLog;
import static utils.Logger.getTimeStamp;
import static utils.Logger.requestLog;
import static utils.Logger.responseLog;

/**
 * Client class that is used to interact with a server and invoke remote methods
 */
public class Client {
  private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private static final Queue<KeyValuePacket> failedRequest = new LinkedBlockingQueue<>();

  static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
  static PaxosAPI api;
  static Map<String, PaxosAPI> serverMap = new HashMap<>();

  /**
   * Driver method of the Client class.
   * Sets up all the servers to communicate with from the servers.properties file
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    monitor();

    try {

      Logger.printMsg(getTimeStamp() + " Establishing communication with servers...");

      Map<String, String> serverList = ServerListFetcher.fetchServers();
      for (String key : serverList.keySet()) {
        String[] server = serverList.get(key).split(":");
        String host = server[0];
        int port = Integer.parseInt(server[1]);
        serverMap.put(key, getServer(host, port));
      }

      Logger.printMsg(getTimeStamp() + " Successfully connected to all servers.");
      String firstServerKey = serverMap.keySet().stream().findFirst().get();

      api = serverMap.get(firstServerKey);

      addDefaultKeyValuePairs();

      startInteractionWithServer();

    } catch (IOException | NotBoundException | InterruptedException e) {
      Logger.errorLog(e.getMessage());
      Logger.printMsg("Please try again...");
      startInteractionWithServer();
    }
  }

  private static void startInteractionWithServer() throws IOException, InterruptedException {
    boolean exit = false;
    while (!exit) {
      exit = getOperationUI();
    }
  }

  /**
   * Get a specific PaxosAPI object from rmi
   *
   * @param host host of registry
   * @param port port of registry
   * @return PaxosAPI object (server)
   * @throws RemoteException   exception
   * @throws NotBoundException exception
   */
  private static PaxosAPI getServer(String host, int port) throws RemoteException, NotBoundException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    return (PaxosAPI) registry.lookup("PaxosAPI");
  }

  /**
   * Presents a UI to the user for key value store operations
   *
   * @return boolean representing if UI to be presented or not.
   * @throws IOException exception
   */
  private static boolean getOperationUI() throws IOException {
    String requestTime = "";
    String request = "";
    String response = "";

    printMenu();
    String op = br.readLine().trim();
    KeyValuePacket packet = null;

    try {
      switch (op) {
        case "1": {
          String key = getKey();
          request = "Get " + key;
          requestTime = getTimeStamp();
          packet = new KeyValuePacket(Type.GET, key, null);
          response = api.get(packet);

          break;
        }
        case "2": {
          String key = getKey();
          String value = getValue();
          request = "PUT (" + key + ", " + value + ")";
          requestTime = getTimeStamp();
          packet = new KeyValuePacket(Type.PUT, key, value);
          response = api.put(packet);

          break;
        }
        case "3": {
          String key = getKey();
          request = "Delete " + key;
          requestTime = getTimeStamp();
          packet = new KeyValuePacket(Type.DELETE, key, null);
          response = api.delete(packet);

          break;
        }
        case "4": {
          api = getServerByUserChoice();
          request = "Change server to:" + api.getName();
          response = "Sever changed to: " + api.getName();
          break;
        }
        case "5": {
          api.saveFile();
          return true;
        }
        default:
          Logger.printMsg("Please choose a valid operation.");
          break;
      }

      requestLog(requestTime, request);
      responseLog(response);

    } catch (RuntimeException e) {
      errorLog("1 or more server(s) may be unreachable..." + e.getMessage());
      errorLog("Check if server id/ map request is valid. View ReadMe for the program...");
      if(packet != null)
        failedRequest.add(packet);

      return false;
    }

    return false;
  }

  private static void printMenu() {
    System.out.print("Operation List: \n1. Get\n2. Put\n3. Delete\n" +
            "4. Change server\n5. Save and Exit\n" +
            "Choose operation: ");
  }

  private static String getKey() throws IOException {
    System.out.print("Enter key: ");
    return br.readLine().trim();
  }

  private static String getValue() throws IOException {
    System.out.print("Enter Value: ");
    return br.readLine().trim();
  }

  /**
   * Asks user the id of the server they wish to communicate with.
   *
   * @return PaxosAPI class (server)
   * @throws IOException exception
   */
  private static PaxosAPI getServerByUserChoice() throws IOException {
    Logger.printMsg("Choose one of the available servers ids...");

    for (String serverId : serverMap.keySet()) {
      Logger.printMsg("ID: " + serverId + ", Address => " + serverMap.get(serverId).getName());
    }

    System.out.print("Enter server id: ");
    String id = br.readLine().trim();
    if (serverMap.containsKey(id)) {
      System.out.println("Choosing: " + id);
      return serverMap.get(id);
    } else {
      Logger.printMsg("Please choose a valid server");
      return api;
    }
  }

  /**
   * Adds default key value pairs to the key value store
   *
   * @throws IOException exception
   */
  private static void addDefaultKeyValuePairs() throws IOException {
    try {
      api.put(new KeyValuePacket(Type.PUT, "hello", "world"));
      api.put(new KeyValuePacket(Type.PUT, "CS6650", "Building Scalable Distributed System"));
      api.put(new KeyValuePacket(Type.PUT, "MS", "Computer Science"));
      api.put(new KeyValuePacket(Type.PUT, "Firstname Lastname", "John Doe"));
      api.put(new KeyValuePacket(Type.PUT, "BTC", "Bitcoin"));
    } catch (RuntimeException e) {

    }

  }

  public static void monitor() {
    executorService.scheduleAtFixedRate(() -> {
      // if a failed request present
      while (!failedRequest.isEmpty()) {
        KeyValuePacket packet = failedRequest.poll();
        // choose another server to retry

        PaxosAPI randomAPI = getRandomServerAPI();

        try {
          Logger.printMsg("Retrying " + packet + " to a different server");
          if (Type.PUT.equals(packet.getType())) {
            randomAPI.put(packet);
          } else if (Type.DELETE.equals(packet.getType())) {
            randomAPI.delete(packet);
          }
        } catch (RemoteException e) {
          Logger.errorLog("Request + " + packet +  " failed again");

          // add the request back to the queue if failed again
          failedRequest.add(packet);
        }
      }
    }, 0, 2000, TimeUnit.MILLISECONDS);
  }

  private static PaxosAPI getRandomServerAPI() {
    Set<String> keySet = serverMap.keySet();
    List<String> keyList = new ArrayList<>(keySet);

    int size = keyList.size();
    int randIdx = new Random().nextInt(size);

    String randomKey = keyList.get(randIdx);
    return serverMap.get(randomKey);
  }
}


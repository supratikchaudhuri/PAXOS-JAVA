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
  static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
  static List<PaxosAPI> servers = new ArrayList<>();
  static PaxosAPI api;
//  static Map<String, String> serverList;
  static Map<String, PaxosAPI> serverMap = new HashMap<>();

  /**
   * Driver method of the Client class
   *
   * @param args accepts command line arguments
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    try {
      if (args.length == 0 || args.length % 2 != 0 || Integer.parseInt(args[1]) > 65535) {
        throw new IllegalArgumentException("Invalid arguments. " +
                "Please provide valid IPs and PORT (0-65535) numbers and start again.");
      }

      System.setProperty("sun.rmi.transport.tcp.responseTimeout", "30000");
      System.setProperty("sun.rmi.transport.tcp.connectionTimeout", "30000");

      Logger.printMsg(getTimeStamp() + " Establishing communication with servers...");

      Map<String, String> serverList = ServerListFetcher.fetchServers();
      for(String key: serverList.keySet()) {
        String[] server = serverList.get(key).split(":");
        String host = server[0];
        int port = Integer.parseInt(server[1]);
        serverMap.put(key, getServer(host, port));
      }

      Logger.printMsg(getTimeStamp() + " Successfully connected to all servers.");
      String firstServerKey = serverMap.keySet().stream().findFirst().get();

      api = serverMap.get(firstServerKey);

      startInteractionWithServer();

    } catch (IOException | NotBoundException | InterruptedException e) {
      Logger.errorLog(e.getMessage());
      Logger.printMsg("Please try again...");
      startInteractionWithServer();
    }
  }

  private static void startInteractionWithServer() throws IOException, InterruptedException {
    addDefaultKeyValuePairs();
    boolean exit = false;
    while (!exit) {
      exit = getOperationUI();
    }
  }

  private static PaxosAPI getServer(String host, int port) throws RemoteException, NotBoundException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    return (PaxosAPI) registry.lookup("PaxosAPI");
  }

  private static boolean getOperationUI() throws IOException {
    String requestTime = "";
    String request = "";
    String response = "";

    printMenu();
    String op = br.readLine().trim();

    try {
      switch (op) {
        case "1": {
          String key = getKey();
          request = "Get " + key;
          requestTime = getTimeStamp();
          KeyValuePacket packet = new KeyValuePacket(Type.GET, key, null);
          response = api.get(packet);

          break;
        }
        case "2": {
          String key = getKey();
          String value = getValue();
          request = "PUT (" + key + ", " + value + ")";
          requestTime = getTimeStamp();
          KeyValuePacket  packet = new KeyValuePacket(Type.PUT, key, value);
          response = api.put(packet);

          break;
        }
        case "3": {
          String key = getKey();
          request = "Delete " + key;
          requestTime = getTimeStamp();
          KeyValuePacket packet = new KeyValuePacket(Type.DELETE, key, null);
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

  private static PaxosAPI getServerByUserChoice() throws IOException {
    Logger.printMsg("Choose one of the available servers ids...");

    for(String serverId: serverMap.keySet()) {
      Logger.printMsg("ID: " + serverId + ", Address => " + serverMap.get(serverId).getName());
    }

    System.out.print("Enter server id: ");
    String id = br.readLine().trim();
    if(serverMap.containsKey(id)) {
      System.out.println("Choosing: " + id);
      return serverMap.get(id);
    }
    else {
      Logger.printMsg("Please choose a valid server");
      return api;
    }
  }

  private static void addDefaultKeyValuePairs() throws IOException, InterruptedException {
    api.put(new KeyValuePacket(Type.PUT, "hello", "world"));
    api.put(new KeyValuePacket(Type.PUT, "CS6650", "Building Scalable Distributed System"));
    api.put(new KeyValuePacket(Type.PUT, "MS", "Computer Science"));
    api.put(new KeyValuePacket(Type.PUT, "Firstname Lastname", "John Doe"));
    api.put(new KeyValuePacket(Type.PUT, "BTC", "Bitcoin"));
  }
}


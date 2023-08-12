package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import utils.Logger;

public class ServerDriver {
  public static void main(String[] args) {
    try {
      if (args.length != 2) {
        throw new IllegalArgumentException("Exactly 2 arguments required, \"<server_ip> <server_port>\"");
      }
      System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
      System.setProperty("sun.rmi.transport.tcp.connectionTimeout", "5000");

      String host = args[0];
      int port = Integer.parseInt(args[1]);

      Map<String, String> serverList = new HashMap<>();
      serverList.put("server1", "localhost:5001");
      serverList.put("server2", "localhost:5002");
      serverList.put("server3", "localhost:5003");
      serverList.put("server4", "localhost:5004");
      serverList.put("server5", "localhost:5005");

      Registry registry = LocateRegistry.createRegistry(port);
      PaxosAPI stub = new Server(serverList, port);
      registry.bind("PaxosAPI", stub);

    } catch (IllegalArgumentException iae) {
      Logger.errorLog(iae.getMessage());
    } catch (RemoteException | AlreadyBoundException e) {
      Logger.errorLog("Cannot register at given port, please modify the configuration file");
    } catch (NotBoundException e) {
      Logger.errorLog("Cannot connect to Proposer helper with given the hostname and port");
    }
  }

}


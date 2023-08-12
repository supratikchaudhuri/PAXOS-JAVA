package server;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import utils.Logger;
import utils.ServerListFetcher;

/**
 * Driver class to start a server at a specific host and port
 */
public class ServerDriver {
  /**
   * Runner function of the class
   *
   * @param args host and port of the server instance.
   */
  public static void main(String[] args) {
    try {
      Map<String, String> serverList = ServerListFetcher.fetchServers();

      if (args.length != 2) {
        throw new IllegalArgumentException("Exactly 2 arguments required, \"<server_ip> <server_port>\"");
      }

      String host = args[0];
      int port = Integer.parseInt(args[1]);

      System.setProperty("java.rmi.server.hostname", host);
      System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
      System.setProperty("sun.rmi.transport.tcp.connectionTimeout", "5000");

      Registry registry = LocateRegistry.createRegistry(port);
      PaxosAPI stub = new Server(serverList, host, port);
      registry.bind("PaxosAPI", stub);

    } catch (IllegalArgumentException iae) {
      Logger.errorLog(iae.getMessage());
    } catch (RemoteException | AlreadyBoundException e) {
      Logger.errorLog("Problem connecting to server. Please check if server address in servers.properties file are valid/free");
    } catch (NotBoundException e) {
      Logger.errorLog("Cannot connect to Proposer helper with given the hostname and port");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}


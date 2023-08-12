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
import java.util.Map;

import utils.Logger;

public class ServerDriver {
  public static void main(String[] args) {
    try {
      if (args.length != 1) {
        throw new IllegalArgumentException("Please specify the <server #No.>");
      }
      String server = args[0];
      Logger.printMsg("Loading server configuration file ...");
      InputStream resource = ServerDriver.class.getClassLoader().getResourceAsStream("serverConfig.yml");
      if (resource == null) {
        throw new FileNotFoundException();
      }
      Yaml yaml = new Yaml();
      Map<String, String> data = yaml.load(resource);
      String serverInfo = data.get(server);
      resource.close();
      if (serverInfo == null) {
        throw new IllegalArgumentException("Invalid input, please try with server<No.>");
      }
      // parse hostname and port
      String[] split = serverInfo.split(":");
      int port = Integer.parseInt(split[1]);
      // registry
      // set timeout
      System.setProperty("sun.rmi.transport.tcp.responseTimeout", "200");
      Registry registry = LocateRegistry.createRegistry(port);
      PaxosAPI stub = new Server(data, port);
      registry.bind("PaxosAPI", stub);
    } catch (FileNotFoundException e) {
      Logger.errorLog("serverConfig.yml file is not found");
    } catch (IllegalArgumentException iae) {
      Logger.errorLog(iae.getMessage());
    } catch (RemoteException | AlreadyBoundException e) {
      Logger.errorLog("Cannot register at given port, please modify the configuration file");
    } catch (UnknownHostException | NotBoundException e) {
      Logger.errorLog("Cannot connect to Proposer helper with given the hostname and port");
    } catch (IOException e) {
      Logger.errorLog("Failed to close the configuration file");
    }
  }

}


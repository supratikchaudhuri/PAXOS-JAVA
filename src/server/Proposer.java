package server;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import utils.KeyValuePacket;
import utils.Logger;
import utils.Request;
import utils.Type;

/**
 * Proposer class that proposes a change in the shared resource shared by multiple server instances.
 */
public class Proposer {
  private final List<Request> promises = new ArrayList<>();
  private final Map<String, String> serverList;
  private final int port;
  private double proposalId;
  private KeyValuePacket value;
  private int acks;
  private String response = "";
  private boolean isDown = false; // phase 1 crash

  public Proposer(Map<String, String> serverList, int port) {
    this.serverList = serverList;
    this.port = port;
  }

  /**
   * Propose a client request to acceptors and synchronously hear from response from acceptors within the timeout.
   *
   * @param clientRequest client request
   * @return response to the request
   */
  public synchronized String propose(KeyValuePacket clientRequest) {
    acks = 0;
    promises.clear();
    
    // ..................... PHASE 1 .....................//
    value = clientRequest;
    proposalId = getProposalID();
    
    // broadcast prepare to all server
    Logger.proposerLog("Start a proposal No. " + proposalId + ", value: " + value);
    serverList.forEach((key, val) -> {
      String[] server = parser(val);
      String host = server[0];
      int port = Integer.parseInt(server[1]);

      try {
        PaxosAPI api = (PaxosAPI) connect(host, port, "PaxosAPI");
        Request res = api.prepare(proposalId);
        // if a promise
        if (res != null && res.getMessageType().equals(Type.PROMISE)) {
          promises.add(res);
        }
      } catch (RemoteException | NotBoundException e) {
        Logger.errorLog("Cannot connect to server with host: " + host + ", port: " + port + " at prepare phase");
      }
    });

    // checking for failure
    if (isDown) {
      response = "Could not propose request. Proposer is down";
      Logger.errorLog(response);
//      crash();
      return response;
    }

    // check if it gets majority support
    if (promises.size() > (serverList.size() / 2)) {
      Logger.proposerLog(promises.size() + " servers replied with promises");
      value = getMaximumAcceptedValue();

      // ================== phase2 ==================//
      // send accept to all acceptors
      Logger.proposerLog("Starting accept phase for the proposal ID: " + proposalId + ", value: " + value);
      serverList.forEach((key, val) -> {
        String[] server = parser(val);
        String host = server[0];
        int port = Integer.parseInt(server[1]);

        try {
          PaxosAPI api = (PaxosAPI) connect(host, port, "PaxosAPI");
          Request res = api.accept(new Request(proposalId, Type.ACCEPT_REQUEST, value));

          if (res != null && res.getMessageType().equals(Type.ACCEPT_RESPONSE)) {
            acks++;
          }
        } catch (RemoteException | NotBoundException e) {
          Logger.errorLog("Cannot connect to server with host: " + host + ", port: " + port + " at accept request phase");
        }
      });
    } else {
      response = "Failed to reach majority consensus. " + promises.size() + "/" + serverList.size() + " supported transaction";
      Logger.errorLog(response);
      return response;
    }

    // check if it gets majority support
    if (acks > (serverList.size() / 2)) {
      Logger.proposerLog(acks + " servers accepted the proposal request");
      // send commit request to all learners
      serverList.forEach((key, val) -> {
        String[] server = parser(val);
        String host = server[0];
        int port = Integer.parseInt(server[1]);

        try {
          PaxosAPI api = (PaxosAPI) connect(host, port, "PaxosAPI");
          response = api.commit(new Request(proposalId, Type.ACCEPT_REQUEST, value));
        } catch (RemoteException | NotBoundException e) {
          Logger.errorLog("Cannot connect to server with host: " + host + ", port: " + port + " at commit phase");
        }
      });
    } else {
      response = "Failed to reach consensus. " + acks + "/" + serverList.size() + " supported transaction";
      Logger.proposerLog(response);
      Logger.proposerLog("Retry proposal request: " + clientRequest);
      return propose(clientRequest);
    }

    // send the Paxos round completion signal to all acceptors
    serverList.forEach((key, val) -> {
      String[] info = parser(val);
      String host = info[0];
      int port = Integer.parseInt(info[1]);
      try {
        // get other node api
        PaxosAPI api = (PaxosAPI) connect(host, port, "PaxosAPI");
        api.onClose();
      } catch (RemoteException | NotBoundException e) {
        Logger.errorLog("Cannot connect to server with host: " + host + ", port: " + port + " at paxos end phase");
      }
    });

    Logger.proposerLog("Proposal with id: " + proposalId + ", request : " + clientRequest + " finished");
    return response;
  }

  /**
   * Fabricating a crash state by causing RMI timeout.
   */
  public void crash() {
    try {
      Thread.sleep(10000);
    } catch (InterruptedException ignore) {
    }
  }

  /**
   * Marking if proposer is down or not
   *
   * @param down boolean
   */
  public void setDown(boolean down) {
    isDown = down;
  }

  /**
   * Get unique proposal number from timestamp and the server port combination.
   * We assume that the chances of different host issuing same ports are nil.
   *
   * @return generated proposal ID
   */
  public double getProposalID() {
    return Double.parseDouble(System.currentTimeMillis() + "." + port);
  }

  /**
   * If acceptors sent promises with values, then return the value with the maximum accepted proposal ID.
   *
   * @return value with the maximum accepted proposal ID or current value
   */
  public KeyValuePacket getMaximumAcceptedValue() {
    List<Request> list = promises.stream().filter(message -> message.getValue() != null).sorted(Comparator.comparingDouble(Request::getProposalId).reversed()).collect(Collectors.toList());
    return list.isEmpty() ? value : list.get(0).getValue();
  }

  /**
   * get remote api
   *
   * @param host host
   * @param port port
   * @return api
   */
  public Remote connect(String host, int port, String apiName) throws RemoteException, NotBoundException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    return registry.lookup(apiName);
  }

  /**
   * split server host and port
   *
   * @param s string to be parsed
   * @return an array contain host and port
   */
  public String[] parser(String s) {
    return s.split(":");
  }

}


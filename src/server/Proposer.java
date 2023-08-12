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
import utils.Message;
import utils.Type;

public class Proposer {

  // collect promises
  private final List<Message> promises = new ArrayList<>();

  // servers information
  private final Map<String, String> serverList;
  private final int port;
  // record proposal number
  private double proposalNum;
  // record proposal value
  private KeyValuePacket value;
  // collect acks of acceptors when accepted a value
  private int acks;
  // response to the client request
  private String response = "";
  // phase 1 crash
  private boolean isDown = false;

  public Proposer(Map<String, String> serverList, int port) {
    this.serverList = serverList;
    this.port = port;
  }

  /**
   * Propose a client request, synchronously hear from response from acceptors within the timeout.
   *
   * @param clientMessage client request
   * @return response to the request
   */
  public synchronized String propose(KeyValuePacket clientMessage) {
    acks = 0;
    promises.clear();
    // ================== phase1 ==================//
    value = clientMessage;
    proposalNum = getProposalNum();
    // broadcast prepare to all server
    Logger.printMsg("Start a proposal No. " + proposalNum + ", value: " + value);
    serverList.forEach((key, val) -> {
      String[] info = parser(val);
      String hostname = info[0];
      int port = Integer.parseInt(info[1]);

      try {
        // get other node api
        PaxosAPI api = (PaxosAPI) connect(hostname, port, "PaxosAPI");
        // send prepare message to them
        Message res = api.prepare(proposalNum);
        // if a promise
        if (res != null && res.getMessageType().equals(Type.PROMISE)) {
          promises.add(res);
        }
      } catch (RemoteException | NotBoundException e) {
        Logger.errorLog("Cannot connect to server with hostname: " + hostname + ", port: " + port + " at prepare phase");
      }
    });
    // fail
    if (isDown) {
      Logger.errorLog("Proposer failed at phase 1 when processing the request: " + clientMessage);
      crash();

    }

    // check if it gets majority support
    if (promises.size() > (serverList.size() / 2)) {
      Logger.printMsg(promises.size() + " servers replied promises");
      // check if a promise has an accepted value
      value = getMaximumAcceptedValue();


      // ================== phase2 ==================//
      // send accept to all acceptors
      Logger.printMsg("Start an accept processing for the proposal No. " + proposalNum + ", value: " + value);
      serverList.forEach((key, val) -> {
        String[] info = parser(val);
        String hostname = info[0];
        int port = Integer.parseInt(info[1]);
        try {
          // get other node api
          PaxosAPI api = (PaxosAPI) connect(hostname, port, "PaxosAPI");
          // send accept message to them
          Message res = api.accept(new Message(proposalNum, Type.ACCEPT_REQUEST, value));
          // if a promise
          if (res != null && res.getMessageType().equals(Type.ACCEPT_RESPONSE)) {
            acks++;
          }
        } catch (RemoteException | NotBoundException e) {
          Logger.errorLog("Cannot connect to server with hostname: " + hostname + ", port: " + port + " at accept request phase");
        }
      });
    } else {
      response = "The proposal message: " + clientMessage + " failed to reach consensus, due to only " + promises.size() + " supports";
      Logger.errorLog(response);
      return response;
    }

    // check if it gets majority support
    if (acks > (serverList.size() / 2)) {
      Logger.printMsg(acks + " servers accepted the proposal message");
      // send commit request to all learners
      serverList.forEach((key, val) -> {
        String[] info = parser(val);
        String hostname = info[0];
        int port = Integer.parseInt(info[1]);
        try {
          // get other node api
          PaxosAPI api = (PaxosAPI) connect(hostname, port, "PaxosAPI");
          // send commit message to them
          response = api.commit(new Message(proposalNum, Type.ACCEPT_REQUEST, value));
        } catch (RemoteException | NotBoundException e) {
          Logger.errorLog("Cannot connect to server with hostname: " + hostname + ", port: " + port + " at commit phase");
        }
      });
    } else {
      response = "The proposal message: " + clientMessage + " failed to reach consensus, due to only " + acks + " supports";
      Logger.printMsg(response);
      Logger.printMsg("Retry with the proposal message: " + clientMessage);
      return propose(clientMessage);
    }

    // send the Paxos round end signal to all acceptors
    serverList.forEach((key, val) -> {
      String[] info = parser(val);
      String hostname = info[0];
      int port = Integer.parseInt(info[1]);
      try {
        // get other node api
        PaxosAPI api = (PaxosAPI) connect(hostname, port, "PaxosAPI");
        api.onClose();
      } catch (RemoteException | NotBoundException e) {
        Logger.errorLog("Cannot connect to server with hostname: " + hostname + ", port: " + port + " at paxos end phase");
      }
    });


    Logger.printMsg("Proposal with id: " + proposalNum + ", request message: " + clientMessage + " done");
    return response;
  }

  /**
   * Make the thread to sleep, this will cause the proposer fail and response timeout.
   */
  public void crash() {
    try {
      Thread.sleep(5000);
    } catch (InterruptedException ignored) {
    }
  }

  /**
   * Down at phase1.
   *
   * @param down boolean
   */
  public void setDown(boolean down) {
    isDown = down;
  }

  /**
   * Get unique proposal number from timestamp and the server port combination.
   * assume servers will run on the different ports
   *
   * @return the current proposal number
   */
  public double getProposalNum() {
    return Double.parseDouble(System.currentTimeMillis() + "." + port);
  }


  /**
   * If acceptors presented promises with values, then the method is to return the value with the maximum accepted proposal number.
   *
   * @return the value with the maximum accepted proposal number or current value
   */
  public KeyValuePacket getMaximumAcceptedValue() {
    List<Message> list = promises.stream().filter(message -> message.getValue() != null).sorted(Comparator.comparingDouble(Message::getProposalNum).reversed()).collect(Collectors.toList());
    return list.isEmpty() ? value : list.get(0).getValue();
  }

  /**
   * get remote api
   *
   * @param hostname hostname
   * @param port     port
   * @return api
   */
  public Remote connect(String hostname, int port, String apiName) throws RemoteException, NotBoundException {
    Registry registry = LocateRegistry.getRegistry(hostname, port);
    return registry.lookup(apiName);
  }

  /**
   * split server hostname and port
   *
   * @param s string to be parsed
   * @return an array contain hostname and port
   */
  public String[] parser(String s) {
    return s.split(":");
  }

}


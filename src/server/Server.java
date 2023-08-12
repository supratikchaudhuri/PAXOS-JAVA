package server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import utils.KeyValuePacket;
import utils.Logger;
import utils.Request;

/**
 * Server class to ship all the methods to be used by clients to store key value pairs in map.
 */
public class Server extends UnicastRemoteObject implements PaxosAPI {
  private final Proposer proposer;
  private final Acceptor acceptor;
  private final Learner learner;
  String name;

  /**
   * Constructor for servers instance.
   *
   * @param serverList list of servers
   * @param host       host of the server
   * @param port       port of the server
   * @throws IOException       exception
   * @throws NotBoundException exception
   */
  public Server(Map<String, String> serverList, String host, int port) throws IOException, NotBoundException {
    name = host + ":" + port;
    this.proposer = new Proposer(serverList, port);
    this.acceptor = new Acceptor();
    this.learner = new Learner();
    CrashGenerator crashGenerator = new CrashGenerator(this.proposer, this.acceptor);
    start(crashGenerator);
    Logger.printMsg("Server started ...\n");
  }

  /**
   * Start a daemon (background) thread.
   *
   * @param object the runnable object to be started in the background
   */
  public void start(Runnable object) {
    Thread thread = new Thread(object);
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public String get(KeyValuePacket message) throws RemoteException {
    Logger.printMsg("Received GET request => key: " + message.getKey());
    String res = learner.commit(message);
    Logger.printMsg("GET response => " + res);
    Logger.printMsg("-----------------------------------------------------------------------\n\n");
    return res;
  }

  @Override
  public String put(KeyValuePacket message) throws RemoteException {
    Logger.printMsg("Received PUT request => key  " + message.getKey() + ", value: " + message.getValue());
    String res = proposer.propose(message);
    Logger.printMsg("PUT response => " + res);
    Logger.printMsg("-----------------------------------------------------------------------\n\n");
    return res;
  }

  @Override
  public String delete(KeyValuePacket message) throws RemoteException {
    Logger.printMsg("Received DELETE request => key: " + message.getKey() + ", value: " + message.getValue());
    String res = proposer.propose(message);
    Logger.printMsg("DELETE response => " + res);
    Logger.printMsg("-----------------------------------------------------------------------\n\n");
    return res;
  }

  @Override
  public Request prepare(double proposeNum) throws RemoteException {
    return acceptor.prepare(proposeNum);
  }

  @Override
  public Request accept(Request message) throws RemoteException {
    return acceptor.accept(message);
  }

  @Override
  public void onClose() throws RemoteException {
    acceptor.resetProposalAccepted();
  }

  @Override
  public String commit(Request message) throws RemoteException {
    Logger.printMsg("Commit proposal No. " + message.getProposalId() + ", value: " + message.getRequest());
    return learner.commit(message.getValue());
  }

  @Override
  public String getName() throws RemoteException {
    return name;
  }

  @Override
  public void saveFile() throws IOException {
    learner.saveMapToFile();
  }
}


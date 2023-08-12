package server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import utils.KeyValuePacket;
import utils.Logger;
import utils.Message;

public class Server extends UnicastRemoteObject implements PaxosAPI {

  private final Proposer proposer;

  private final Acceptor acceptor;

  private final Learner learner;

  protected Server(Map<String, String> serverList, int port) throws RemoteException, UnknownHostException, NotBoundException {
    this.proposer = new Proposer(serverList, port);
    this.acceptor = new Acceptor();
    this.learner = new Learner();
    CrashGenerator crashGenerator = new CrashGenerator(this.proposer, this.acceptor);
    start(crashGenerator);
    Logger.printMsg("Server started ...\n");
  }


  /**
   * Start a daemon thread.
   *
   * @param object the runnable object to be started
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
  public Message prepare(double proposeNum) throws RemoteException {
    return acceptor.prepare(proposeNum);
  }

  @Override
  public Message accept(Message message) throws RemoteException {
    return acceptor.accept(message);
  }

  @Override
  public void onClose() throws RemoteException {
    acceptor.resetProposalAccepted();
  }

  @Override
  public String commit(Message message) throws RemoteException {
    Logger.printMsg("Commit proposal No. " + message.getProposalId() + ", value: " + message.getRequest());
    return learner.commit(message.getValue());
  }

  @Override
  public String getName() throws RemoteException {
    return "my _ name";
  }
}


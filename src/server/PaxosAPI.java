package server;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import utils.KeyValuePacket;
import utils.Request;

/**
 * The api to ship server RMI methods by clients.
 */
public interface PaxosAPI extends Remote {

  /**
   * Receive get request from the client and query the key value to send the response.
   *
   * @param request client key value request
   * @return response form the server
   * @throws RemoteException exception during rmi connection/ method invocation
   */
  String get(KeyValuePacket request) throws RemoteException;

  /**
   * Receive put request from the client and store the key value and send the response.
   *
   * @param request client key value request
   * @return response form the server
   * @throws RemoteException exception during rmi connection/ method invocation
   */
  String put(KeyValuePacket request) throws RemoteException;

  /**
   * Receive delete request from the client and delete the key value and send the response.
   *
   * @param request client key value request
   * @return response form the server
   * @throws RemoteException exception during rmi connection/ method invocation
   */
  String delete(KeyValuePacket request) throws RemoteException;

  /**
   * Used to starts a Paxos prepare request.
   *
   * @param proposalId the proposal ID form the proposer
   * @return promise for the proposal or null if rejected
   * @throws RemoteException exception during rmi connection/ method invocation
   */
  Request prepare(double proposalId) throws RemoteException;

  /**
   * Used to starts a Paxos accept request.
   *
   * @param request the request passed at phase 1
   * @return ack accept or null if the proposal number smaller than current
   * @throws RemoteException exception during rmi connection/ method invocation
   */
  Request accept(Request request) throws RemoteException;

  /**
   * Use by the acceptor to end the current Paxos round.
   *
   * @throws RemoteException exception during rmi connection/ method invocation
   */
  void onClose() throws RemoteException;

  /**
   * Used by leaner to commit the chosen client request.
   *
   * @param request the chosen request
   * @return response after commit process
   * @throws RemoteException exception during rmi connection/ method invocation
   */
  String commit(Request request) throws RemoteException;

  /**
   * Returns the names representing the server address
   *
   * @return name
   * @throws RemoteException exception during rmi connection/ method invocation
   */
  String getName() throws RemoteException;

  /**
   * Stores key value store to offline map.properties file
   *
   * @throws IOException accessing file from the project folder
   */
  void saveFile() throws IOException;
}

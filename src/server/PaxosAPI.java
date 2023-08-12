package server;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import utils.KeyValuePacket;
import utils.Request;

/**
 * The api of Paxos consensus.
 */
public interface PaxosAPI extends Remote {

  /**
   * Receive get request from the client and query the key value to send the response.
   *
   * @param message client key value message
   * @return response
   * @throws RemoteException thrown when cannot establish connection
   */
  String get(KeyValuePacket message) throws RemoteException;

  /**
   * Receive put request from the client and store the key value and send the response.
   *
   * @param message client key value message
   * @return response
   * @throws RemoteException thrown when cannot establish connection or the service is unavailable at proposer or acceptor
   */
  String put(KeyValuePacket message) throws RemoteException;

  /**
   * Receive delete request from the client and delete the key value and send the response.
   *
   * @param message client key value message
   * @return response
   * @throws RemoteException thrown when cannot establish connection or the service is unavailable at proposer or acceptor
   */
  String delete(KeyValuePacket message) throws RemoteException;

  /**
   * Use for proposer starts a Paxos and requires acceptor to prepare.
   *
   * @param proposalNum the proposal number
   * @return promise of the proposal or null if rejected
   * @throws RemoteException thrown when cannot establish connection or the service is unavailable at proposer or acceptor
   */
  Request prepare(double proposalNum) throws RemoteException;

  /**
   * Use for acceptor to decide whether accept the message.
   *
   * @param message the message passed at phase 1
   * @return ack accept or null if the proposal number smaller than current
   * @throws RemoteException thrown when cannot establish connection or the service is unavailable at proposer or acceptor
   */
  Request accept(Request message) throws RemoteException;

  /**
   * Use for acceptor ends the current Paxos round.
   *
   * @throws RemoteException thrown when the server is not reachable
   */
  void onClose() throws RemoteException;

  /**
   * Use for leaner to commit the chosen message.
   *
   * @param message the chosen message
   * @return response after processing the commit of message
   * @throws RemoteException thrown when cannot establish connection
   */
  String commit(Request message) throws RemoteException;

  String getName() throws  RemoteException;

  void saveFile() throws IOException;
}

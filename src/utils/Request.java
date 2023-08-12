package utils;

import java.io.Serializable;

/**
 * A class to package client request.
 */
public class Request implements Serializable {
  private final double proposalId;
  private final Type requestType;
  private final KeyValuePacket packet;

  /**
   * Constructor for Request class.
   *
   * @param id   proposal Id
   * @param type type of request
   * @param o    Request packet from client
   */
  public Request(double id, Type type, KeyValuePacket o) {
    this.proposalId = id;
    this.requestType = type;
    this.packet = o;
  }

  /**
   * Getter for proposal ID
   *
   * @return proposal id
   */
  public double getProposalId() {
    return proposalId;
  }

  /**
   * Getter for request type by client
   *
   * @return type of request.
   */
  public Type getMessageType() {
    return requestType;
  }

  /**
   * Getter for Request packet.
   *
   * @return request packet received from client
   */
  public KeyValuePacket getValue() {
    return packet;
  }

  /**
   * Readable KeyValuePacket object
   *
   * @return String version of request packet.
   */
  public String getRequest() {
    return packet.toString();
  }

  /**
   * Returns key for KeyValuePacket object
   *
   * @return key
   */
  public String getKey() {
    return packet.getKey();
  }
}

package utils;

import java.io.Serializable;

public class Message implements Serializable {
  private static final long serialVersionUID = 1234567L;

  private final double proposalNum;

  private final Type messageType;

  private final KeyValuePacket value;

  public Message(double maxId, Type promise, KeyValuePacket o) {
    this.proposalNum = maxId;
    this.messageType = promise;
    this.value = o;
  }

  public double getProposalNum() {
    return proposalNum;
  }

  public Type getMessageType() {
    return messageType;
  }

  public KeyValuePacket getValue() {
    return value;
  }
}

package utils;

import java.io.Serializable;

public class Message implements Serializable {
  private static final long serialVersionUID = 1234567L;

  private final double proposalId;

  private final Type messageType;

  private final KeyValuePacket packet;

  public Message(double maxId, Type promise, KeyValuePacket o) {
    this.proposalId = maxId;
    this.messageType = promise;
    this.packet = o;
  }

  public double getProposalId() {
    return proposalId;
  }

  public Type getMessageType() {
    return messageType;
  }

  public KeyValuePacket getValue() {
    return packet;
  }

  public String getKey() {
    return packet.getKey();
  }
}

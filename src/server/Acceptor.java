package server;

import utils.KeyValuePacket;
import utils.Logger;
import utils.Message;
import utils.Type;

public class Acceptor {
  // stop the latest accepted proposal number
  private double acceptedNum;

  // the maximum proposal number
  private double maxId;

  // latest accepted value
  private KeyValuePacket acceptedValue;

  private boolean proposalAccepted = false;

  // crash trigger
  private boolean isDown = false;


  /**
   * Prepare for a proposal number.
   *
   * @param id the proposal number
   * @return if a proposal has been already accepted, return a promise with the accepted number and value;
   * if the proposal is accepted, return a promise without any value;
   * else return nothing to reject the proposal that the number is smaller than the maximum proposal number
   */
  public Message prepare(double id) {
    // fail
    crash();
    if (maxId < id) {
      maxId = id;
      Logger.printMsg("Sent promise to proposal No. " + maxId);
      // check if the proposal has been already accepted
      if (proposalAccepted) {
        return new Message(acceptedNum, Type.PROMISE, acceptedValue);
      }
      // return a promise without any value
      return new Message(maxId, Type.PROMISE, null);
    }
    Logger.errorLog("Reject proposal No. " + id + ", reason: smaller than current proposal number: " + maxId);
    return null;
  }

  /**
   * Accept a promised value.
   *
   * @param message the promised value
   * @return if the value is accepted, return an ack;
   * else return nothing to reject the proposal that the number is smaller than the maximum proposal number
   */
  public Message accept(Message message) {
    // fail
    crash();

    Logger.printMsg("Accepting for proposal No. " + message.getProposalNum());
    if (maxId == message.getProposalNum()) {
      proposalAccepted = true;
      acceptedNum = message.getProposalNum();
      acceptedValue = message.getValue();
      Logger.printMsg("Sent accepted ack to propose No. " + maxId);
      return new Message(maxId, Type.ACCEPT_RESPONSE, null);
    }
    Logger.errorLog("Reject proposal No. " + message.getProposalNum() + ", reason: smaller than current proposal number: " + maxId);
    return null;
  }


  /**
   * Use to end the current paxos round and reset the state, once learners have updated the key value store.
   */
  public void resetProposalAccepted() {
    Logger.printMsg("Proposal No. " + acceptedNum + " round ended");
    this.proposalAccepted = false;
  }

  /**
   * Make the thread to sleep, this will cause the proposer fail and response timeout.
   */
  public void crash() {
    if (isDown) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException ignored) {
      }
    }
  }

  /**
   * To trigger crash.
   *
   * @param down boolean
   */
  public void setDown(boolean down) {
    isDown = down;
  }

}

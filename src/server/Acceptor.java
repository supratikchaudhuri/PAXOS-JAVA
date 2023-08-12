package server;

import utils.KeyValuePacket;
import utils.Logger;
import utils.Message;
import utils.Type;

public class Acceptor {
  private double acceptedId;
  private double maxId;
  private KeyValuePacket acceptedValue;
  private boolean proposalAccepted = false;
  private boolean isDown = false;


  /**
   * Prepare for a proposal id.
   *
   * @param id the proposal id
   * @return if a proposal has already been accepted, return a promise with the accepted ID and value;
   * If the proposal is accepted, return a promise without any value, i.e. null;
   * Else return nothing to indicate rejection of the proposal ID that is smaller than the maximum proposal ID
   */
  public Message prepare(double id) {
    // check for failure
    crash();
    
    if (maxId < id) {
      maxId = id;
      Logger.acceptorLog("Sent promise to proposal ID " + maxId);
      
      // check if any proposal has been already accepted
      if (proposalAccepted) {
        return new Message(acceptedId, Type.PROMISE, acceptedValue);
      }
      // return a promise without any value
      return new Message(maxId, Type.PROMISE, null);
    }
    Logger.acceptorLog("Proposal ID: " + id + " rejected; Max proposal ID seen till now: " + maxId);
    return null;
  }

  /**
   * Accept a promised value.
   *
   * @param message representing a promised value
   * @return If the value is accepted, return an acknowledgement;
   * Else return nothing to indicate rejection of the proposal ID that is smaller than the maximum proposal ID
   */
  public Message accept(Message message) {
    // checks for failure
    crash();

    Logger.acceptorLog("Accepting for proposal ID: " + message.getProposalId());
    if (maxId == message.getProposalId()) {
      proposalAccepted = true;
      acceptedId = message.getProposalId();
      acceptedValue = message.getValue();
      Logger.acceptorLog("Sent 'ACCEPTED' ack to proposal ID: " + maxId);
      return new Message(maxId, Type.ACCEPT_RESPONSE, null);
    }
    Logger.acceptorLog("Reject proposal ID: " + message.getProposalId() + ", reason: smaller than current proposal number: " + maxId);
    return null;
  }


  /**
   * Use to end the current paxos round and reset the state, once learners have updated the key value store.
   */
  public void resetProposalAccepted() {
    Logger.acceptorLog("Proposal ID: " + acceptedId + " round has ended");
    this.proposalAccepted = false;
  }

  /**
   * Fabricating a crash state by causing RMI timeout.
   */
  public void crash() {
    if (isDown) {
      try {
        Thread.sleep(10000);
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

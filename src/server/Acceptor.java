package server;

import utils.KeyValuePacket;
import utils.Logger;
import utils.Request;
import utils.Type;

/**
 * Acceptor class to prepare and commit a proposal from the proposer.
 */
public class Acceptor {
  private double acceptedId;
  private double maxId;
  private KeyValuePacket acceptedValue;
  private boolean proposalAccepted = false;
  private boolean isDown = false;

  /**
   * Prepare for a proposal id.
   *
   * @param id proposal ID presented by proposer
   * @return case 1: If a proposal has already been accepted, return a promise with the accepted ID and value
   * case 2: If the proposal is accepted, return a promise without any value, i.e. null
   * case 3: Else return nothing to indicate rejection of the proposal ID that is smaller than the maximum proposal ID
   */
  public Request prepare(double id) {
    crash();

    if (maxId < id) {
      maxId = id;
      Logger.acceptorLog("Sent promise to proposal ID " + maxId);

      // case 1
      if (proposalAccepted) {
        return new Request(acceptedId, Type.PROMISE, acceptedValue);
      }
      // case 2
      else {
        return new Request(maxId, Type.PROMISE, null);
      }
    }
    // case 3
    Logger.acceptorLog("Proposal ID: " + id + " rejected; Max proposal ID seen till now: " + maxId);
    return null;
  }

  /**
   * Accept a promised value.
   *
   * @param request representing a promised value (request)
   * @return If the value is accepted, return an acknowledgement
   * Else return nothing to indicate rejection of the proposal ID that is smaller than the maximum proposal ID
   */
  public Request accept(Request request) {
    crash();

    Logger.acceptorLog("Accepting for proposal ID: " + request.getProposalId());
    if (maxId == request.getProposalId()) {
      proposalAccepted = true;
      acceptedId = request.getProposalId();
      acceptedValue = request.getValue();
      Logger.acceptorLog("Sent 'ACCEPTED' ack to proposal ID: " + maxId);
      return new Request(maxId, Type.ACCEPT_RESPONSE, null);
    }
    Logger.acceptorLog("Reject proposal ID: " + request.getProposalId() + ", reason: smaller than current proposal number: " + maxId);
    return null;
  }


  /**
   * Use to end the current paxos round and reset the acceptor states after learners have updated the map.
   */
  public void resetProposalAccepted() {
    Logger.acceptorLog("Proposal ID: " + acceptedId + " round has ended");
    this.proposalAccepted = false;
  }

  /**
   * Fabricating a crash state by causing RMI timeout, if isDown = true by user.
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
   * @param down state of acceptor liveliness.
   */
  public void setDown(boolean down) {
    isDown = down;
  }

}

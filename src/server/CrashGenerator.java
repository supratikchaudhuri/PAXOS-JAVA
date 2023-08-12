package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import utils.Logger;

/**
 * A class the enables the user to adjust acceptor and proposer states from CLI by user.
 */
public class CrashGenerator implements Runnable {
  private final Proposer proposer;
  private final Acceptor acceptor;

  /**
   * Constructor to initialize the object whose states can be changed.
   *
   * @param proposer Proposer object
   * @param acceptor Acceptor object
   */
  public CrashGenerator(Proposer proposer, Acceptor acceptor) {
    this.proposer = proposer;
    this.acceptor = acceptor;
  }

  @Override
  public void run() {
    Logger.printMsg("Crash generator started ...\n");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      Logger.printMsg("--------------CRASH MENU-------------");
      Logger.printMsg("Please input command: " +
              "\n0 to suspend the acceptor, \n1 to recover the acceptor, " +
              "\n2 to suspend the proposer, \n3 to recover the proposer" +
              "\nNOTE: Once the suspend function is enabled, the acceptor will fail at either phase 1 or phase 2, the proposer will only fail at phase 1");

      try {
        String command = reader.readLine();
        int i = Integer.parseInt(command);
        switch (i) {
          case 0:
            acceptor.setDown(true);
            Logger.printMsg("Acceptor is down");
            break;
          case 1:
            acceptor.setDown(false);
            Logger.printMsg("Acceptor successfully recovered");
            break;
          case 2:
            proposer.setDown(true);
            Logger.printMsg("Proposer is down");
            break;
          case 3:
            proposer.setDown(false);
            Logger.printMsg("Proposer successfully recovered");
            break;
          default:
            throw new IllegalArgumentException("Invalid command");
        }
      } catch (IOException e) {
        Logger.errorLog(e.getMessage());
        try {
          reader.close();
        } catch (IOException ignored) {
        }
      } catch (IllegalArgumentException iae) {
        Logger.errorLog(iae.getMessage());
      }
    }
  }
}

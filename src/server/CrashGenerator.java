package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import utils.Logger;

public class CrashGenerator implements Runnable {
  private final Proposer proposer;
  private final Acceptor acceptor;

  public CrashGenerator(Proposer proposer, Acceptor acceptor) {
    this.proposer = proposer;
    this.acceptor = acceptor;
  }


  @Override
  public void run() {
    Logger.printMsg("Crash generator started ...");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      Logger.printMsg("Please input command: 0 to suspend the acceptor, 1 to recover the acceptor, ");
      Logger.printMsg("2 to suspend the proposer at phase 1, 3 to recover the proposer");
      Logger.printMsg("Once enable the suspend function, the acceptor will fail at either phase 1 or phase 2, the proposer will only fail at phase 1");
      try {
        String command = reader.readLine();
        int i = Integer.parseInt(command);
        switch (i) {
          case 0:
            Logger.printMsg("Acceptor is down");
            acceptor.setDown(true);
            break;
          case 1:
            acceptor.setDown(false);
            Logger.printMsg("Acceptor recovered");
            break;
          case 2:
            Logger.printMsg("Proposer will be down at phase 1");
            proposer.setDown(true);
            break;
          case 3:
            proposer.setDown(false);
            Logger.printMsg("Proposer recovered");
            break;
          default:
            throw new IllegalArgumentException("Unknown command");
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

package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to streamline printing onto console.
 */
public class Logger {

  /**
   * Prints message on console
   *
   * @param msg string o be printed
   */
  public static void printMsg(String msg) {
    System.out.println(msg);
  }

  /**
   * Returns current timestamp
   *
   * @return string of timestamp
   */
  public static String getTimeStamp() {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    return "[Time: " + sdf.format(new Date()) + "] ";
  }

  /**
   * Prints request message in the console
   *
   * @param req request string to be printed
   */
  public static void requestLog(String req) {
    System.out.println(getTimeStamp() + "REQUEST: " + req);
  }

  /**
   * Prints request message in the console
   *
   * @param timestamp time of request
   * @param req       request string to be printed
   */
  public static void requestLog(String timestamp, String req) {
    System.out.println(timestamp + "REQUEST: " + req);
  }

  /**
   * Prints Response message in the console
   *
   * @param res response string to be printed
   */
  public static void responseLog(String res) {
    System.out.println(getTimeStamp() + "RESPONSE: " + res);
  }

  /**
   * Logs error to the console
   *
   * @param err error message
   */
  public static void errorLog(String err) {
    System.out.println("====================================================================================");
    System.out.println(getTimeStamp() + "ERROR: " + err);
    System.out.println("====================================================================================");
  }
}
package utils;

import java.io.Serializable;

/**
 * Class to package client request packets.
 */
public class KeyValuePacket implements Serializable {
  private static final long serialVersionUID = 1234567L;
  private final Type type;
  private final String key;
  private final String value;

  /**
   * Constructor to initialize KeyValuePacket object.
   *
   * @param type  type of request
   * @param key   key
   * @param value value
   */
  public KeyValuePacket(Type type, String key, String value) {
    this.type = type;
    this.key = key;
    this.value = value;
  }

  /**
   * Getter for type of request
   *
   * @return type of request
   */
  public Type getType() {
    return type;
  }

  /**
   * Getter for key of request
   *
   * @return key of request
   */
  public String getKey() {
    return key;
  }

  /**
   * Getter for value of request
   *
   * @return value of request
   */
  public String getValue() {
    return value;
  }

  /**
   * Stringifies the class object to human-readable format.
   *
   * @return string representing class object
   */
  public String toString() {
    String reqType = "";
    if (type == Type.GET) reqType = "GET";
    if (type == Type.PUT) reqType = "PUT";
    if (type == Type.DELETE) reqType = "DELETE";

    return reqType + " (" + key + ", " + value + ")";
  }
}

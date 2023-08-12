package utils;

import java.io.Serializable;

public class KeyValuePacket implements Serializable {

  private static final long serialVersionUID = 1234567L;
  private Type type;
  private String key;
  private String value;

  public KeyValuePacket(Type type, String key, String value) {
    this.type = type;
    this.key = key;
    this.value = value;
  }

  public Type getType() {
    return type;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }


  public String toString() {
    String reqType = "";
    if(type == Type.GET) reqType = "GET";
    if(type == Type.PUT) reqType = "PUT";
    if(type == Type.DELETE) reqType = "DELETE";

    return reqType + " (" + key + ", " + value + ")";
  }
}

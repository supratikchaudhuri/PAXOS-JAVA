package utils;

import java.io.Serializable;

public class KeyValuePacket implements Serializable {

  private static final long serialVersionUID = 1234567L;

  // put or delete
  private Type type;

  private String key;

  private String value;

}

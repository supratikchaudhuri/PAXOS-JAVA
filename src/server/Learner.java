package server;
import DB.DB;
import utils.KeyValuePacket;

public class Learner extends DB {

  /**
   * Use for commit key value pair and modify the storage.
   *
   * @param message the message to be committed
   * @return response
   */
  public String commit(KeyValuePacket message) {
    String res;
    switch (message.getType()) {
      case GET:
        // if the key is existed
        if (isContain(message.getKey())) {
          res = "key: " + message.getKey() + ", value: " + get(message.getKey());
        } else {
          res = "key: " + message.getKey() + " is not found";
        }
        return res;
      case PUT:
        // if the key is existed
        if (isContain(message.getKey())) {
          res = "key: " + message.getKey() + ", value: " + get(message.getKey()) + " is immutable";
        } else {
          put(message.getKey(), message.getValue());
          res = "key: " + message.getKey() + ", value: " + message.getValue() + " has been stored";
        }
        return res;
      case DELETE:
        // if the key is not existed
        if (!isContain(message.getKey())) {
          res = "key: " + message.getKey() + " doesn't exist";
        } else {
          delete(message.getKey());
          res = "key: " + message.getKey() + " has been deleted";
        }
        return res;
      default:
        // if malformed
        return null;
    }
  }
}

